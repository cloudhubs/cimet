package edu.university.ecs.lab.common.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.common.config.models.InputConfig;
import edu.university.ecs.lab.common.config.models.InputRepository;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.intermediate.create.utils.StringParserUtils;
import edu.university.ecs.lab.common.models.*;
import javassist.NotFoundException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Static utility class for parsing a file and returning associated models from code structure. */
public class JsonToObjectUtils {

  /**
   * Parse a Java class file and return a JClass object.
   * The class role will be determined by {@link #parseClassRole(File)} and the returned object
   * will be of correct {@link JClass} subclass type.
   * @param sourceFile the file to parse
   * @return the JClass object representing the file
   * @throws IOException on parse error
   */
  public static JClass parseClass(File sourceFile, InputConfig config) throws IOException {
    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return null;
    }

    JClass jClass = JClass.builder()
            .classPath(getRepositoryPath(sourceFile, config))
            .className(sourceFile.getName().replace(".java", ""))
            .packageName(packageName)
            .methods(parseMethods(cu))
            .fields(parseFields(sourceFile))
            .methodCalls(parseMethodCalls(sourceFile))
            .msId(getServiceName(sourceFile, config))
            .classRole(parseClassRole(sourceFile))
            .build();

    // Handle special class roles
    if (jClass.getClassRole() == ClassRole.CONTROLLER) {
      JController controller = new JController(jClass);
      controller.setEndpoints(parseEndpoints(sourceFile));
      return controller;
    } else if (jClass.getClassRole() == ClassRole.SERVICE) {
      JService service = new JService(jClass);
      service.setRestCalls(parseRestCalls(sourceFile, config));
      return service;
    }

    return jClass;
  }

  /**
   * Get the service name from the given file. This is determined by the file path and config.
   * @param sourceFile the file to parse
   * @return the service name of the file, null if not found
   */
  private static String getServiceName(File sourceFile, InputConfig config) {
    // Get the path beginning with repoName/serviceName/...
    String filePath = getRepositoryPath(sourceFile, config);

    // Find correct repository from config
    for (InputRepository repo : config.getRepositories()) {
      if (filePath.startsWith(repo.getName())) {
        for (String servicePath : repo.getPaths()) {
          // remove repoName/ from the path
          String subPath = filePath.substring(repo.getName().length() + 1);

          if (subPath.startsWith(servicePath)) {
            try {
              return repo.getServiceNameFromPath(servicePath);
            } catch (NotFoundException e) {
              System.err.println("Failed to get service name from path \"" + filePath + "\": " + e.getMessage());
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Get the path from the repository TLD of the file from the clonePath directory.
   * This will look like repoName/serviceName/path/to/file.java
   * @param sourceFile the file to get the path of
   * @param config system input config file
   * @return the relative path of the file after ./clonePath/
   */
  private static String getRepositoryPath(File sourceFile, InputConfig config) {
    // Get the file path start from the clonePath directory
    String filePath = sourceFile.getAbsolutePath();
    String clonePath = config.getClonePath();

    // Sanitize clonePath
    clonePath = clonePath.replace("./", "").replace(".\\", "");

    int clonePathIndex = filePath.indexOf(clonePath);

    if (clonePathIndex == -1) {
      System.err.println("Error: File path does not contain clone path when trying to get relativePath: " + filePath);
      return filePath;
    }

    return filePath.substring(clonePathIndex + clonePath.length() + 1);
  }

  public static List<Method> parseMethods(CompilationUnit cu) {
    List<Method> methods = new ArrayList<>();

    // loop through methods
    for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
      methods.add(parseMethod(md));
    }

    return methods;
  }

  public static List<Endpoint> parseEndpoints(File sourceFile) throws IOException {
    List<Endpoint> endpoints = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      AnnotationExpr aExpr = cid.getAnnotationByName("RequestMapping").orElse(null);

      if (aExpr == null) {
        return endpoints;
      }

      String classLevelPath = pathFromAnnotation(aExpr);

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        Endpoint endpoint = new Endpoint(parseMethod(md));

        // loop through annotations
        for (AnnotationExpr ae : md.getAnnotations()) {
          endpoint.setUrl(StringParserUtils.mergePaths(classLevelPath, pathFromAnnotation(ae)));
          endpoint.setDecorator(ae.getNameAsString());

          switch (ae.getNameAsString()) {
            case "GetMapping":
              endpoint.setHttpMethod("GET");
              break;
            case "PostMapping":
              endpoint.setHttpMethod("POST");
              break;
            case "DeleteMapping":
              endpoint.setHttpMethod("DELETE");
              break;
            case "PutMapping":
              endpoint.setHttpMethod("PUT");
              break;
            case "RequestMapping":
              if (ae.toString().contains("RequestMethod.POST")) {
                endpoint.setHttpMethod("POST");
              } else if (ae.toString().contains("RequestMethod.DELETE")) {
                endpoint.setHttpMethod("DELETE");
              } else if (ae.toString().contains("RequestMethod.PUT")) {
                endpoint.setHttpMethod("PUT");
              } else {
                endpoint.setHttpMethod("GET");
              }
              break;
          }

          if (endpoint.getHttpMethod() == null) {
            continue;
          }

          endpoints.add(endpoint);
        }
      }
    }

    return endpoints;
  }

  public static Method parseMethod(MethodDeclaration md) {
    Method method = new Method();
    method.setMethodName(md.getNameAsString());

    // Get params and returnType
    NodeList<Parameter> parameterList = md.getParameters();
    StringBuilder parameter = new StringBuilder();

    if (parameterList.size() != 0) {
      parameter = new StringBuilder("[");
      for (int i = 0; i < parameterList.size(); i++) {
        parameter.append(parameterList.get(i).toString());
        if (i != parameterList.size() - 1) {
          parameter.append(", ");
        } else {
          parameter.append("]");
        }
      }
    }

    method.setParameterList(parameter.toString());
    method.setReturnType(md.getTypeAsString());

    return method;
  }

  public static List<RestCall> parseRestCalls(File sourceFile, InputConfig config) throws IOException {
    List<RestCall> restCalls = new ArrayList<>();
    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      // loop through methods

      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        String parentMethodName = md.getNameAsString();

        // loop through method calls
        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
          MethodCall methodCall = new MethodCall();
          String methodName = mce.getNameAsString();
          Expression scope = mce.getScope().orElse(null);

          RestCall restCall = RestCall.findCallByName(methodName);
          String calledServiceName = getCalledServiceName(scope);

          // Are we a rest call
          if (!Objects.isNull(restCall)
              && Objects.nonNull(calledServiceName)
              && calledServiceName.equals("restTemplate")) {
            // get http methods for exchange method
            if (restCall.getMethodName().equals("exchange")) {
              restCall.setHttpMethod(getHttpMethodForExchange(mce.getArguments().toString()));
            }

            // TODO find a more graceful way of handling/validating this can be passed up
            if (parseURL(mce, cid).equals("")) {
              continue;
            }

            restCall.setApi(parseURL(mce, cid));
            restCall.setParentMethod(parentMethodName);
            restCall.setCalledFieldName(getCalledServiceName(scope));
            restCall.setSourceFile(
                    getRepositoryPath(sourceFile, config));

            restCalls.add(restCall);
            // System.out.println(restCall);
          }
        }
      }
    }
    return restCalls;
  }

  public static List<MethodCall> parseMethodCalls(File sourceFile) throws IOException {
    CompilationUnit cu = StaticJavaParser.parse(sourceFile);
    List<MethodCall> methodCalls = new ArrayList<>();

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      // loop through methods

      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        String parentMethodName = md.getNameAsString();

        // loop through method calls
        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
          MethodCall methodCall = new MethodCall();
          String methodName = mce.getNameAsString();
          Expression scope = mce.getScope().orElse(null);

          RestCall restCall = RestCall.findCallByName(methodName);
          String calledServiceName = getCalledServiceName(scope);

          // Are we a rest call
          if (!Objects.isNull(restCall)
              && Objects.nonNull(calledServiceName)
              && calledServiceName.equals("restTemplate")) {
            // do nothing, we only want regular methodCalls
            // System.out.println(restCall);
          } else if (Objects.nonNull(calledServiceName)) {
            methodCall.setParentMethod(parentMethodName);
            methodCall.setMethodName(methodName);
            methodCall.setCalledFieldName(getCalledServiceName(scope));

            methodCalls.add(methodCall);
          }
        }
      }
    }
    return methodCalls;
  }

  private static List<Field> parseFields(File sourceFile) throws IOException {
    List<Field> javaFields = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
        for (VariableDeclarator variable : fd.getVariables()) {
          javaFields.add(new Field(variable));
        }
      }
    }

    return javaFields;
  }

  /**
   * Parse the class role of the given file. This is determined by the file name and parent path.
   * @param sourceFile the file to parse
   * @return the {@link ClassRole} of the file
   */
  private static ClassRole parseClassRole(File sourceFile) {
    String fileName = sourceFile.getName().toLowerCase();
    String parentPath = sourceFile.getParent().toLowerCase();

    if (fileName.contains("controller")) {
      return ClassRole.CONTROLLER;
    } else if (fileName.contains("service")) {
      return ClassRole.SERVICE;
    } else if (fileName.contains("dto")) {
      return ClassRole.DTO;
    } else if (fileName.contains("repository")) {
      return ClassRole.REPOSITORY;
    } else if (parentPath.contains("entity") || parentPath.contains("model")) {
      return ClassRole.ENTITY;
    } else {
      return ClassRole.UNKNOWN;
    }
  }

  private static String pathFromAnnotation(AnnotationExpr ae) {
    if (ae == null) {
      return "";
    }

    if (ae.isSingleMemberAnnotationExpr()) {
      return StringParserUtils.removeOuterQuotations(
          ae.asSingleMemberAnnotationExpr().getMemberValue().toString());
    }

    if (ae.isNormalAnnotationExpr() && ae.asNormalAnnotationExpr().getPairs().size() > 0) {
      for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
        if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
          return StringParserUtils.removeOuterQuotations(mvp.getValue().toString());
        }
      }
    }

    return "";
  }

  // TODO is this called service as in microservice? Or as in service class? Service method? Rename to avoid confusion
  private static String getCalledServiceName(Expression scope) {
    String calledServiceID = null;
    if (Objects.nonNull(scope) && scope instanceof NameExpr) {
      NameExpr fae = scope.asNameExpr();
      calledServiceID = fae.getNameAsString();
    }

    return calledServiceID;
  }

  /**
   * Find the URL from the given method call expression.
   *
   * @param mce the method call to extract url from
   * @param cid the class or interface to search
   * @return the URL found
   */
  // TODO: what is URL here? Is it the URL of the service? Or the URL of the method call? Rename to avoid confusion
  private static String parseURL(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
    if (mce.getArguments().isEmpty()) {
      return "";
    }

    Expression exp = mce.getArguments().get(0);

    if (exp.isStringLiteralExpr()) {
      return StringParserUtils.removeOuterQuotations(exp.toString());
    } else if (exp.isFieldAccessExpr()) {
      return parseFieldValue(cid, exp.asFieldAccessExpr().getNameAsString());
    } else if (exp.isNameExpr()) {
      return parseFieldValue(cid, exp.asNameExpr().getNameAsString());
    } else if (exp.isBinaryExpr()) {
      return parseUrlFromBinaryExp(exp.asBinaryExpr());
    }

    return "";
  }

  private static String parseFieldValue(ClassOrInterfaceDeclaration cid, String fieldName) {
    for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
      if (fd.getVariables().toString().contains(fieldName)) {
        Expression init = fd.getVariable(0).getInitializer().orElse(null);
        if (init != null) {
          return StringParserUtils.removeOuterQuotations(init.toString());
        }
      }
    }

    return "";
  }

  // TODO: kind of resolved, probably not every case considered
  private static String parseUrlFromBinaryExp(BinaryExpr exp) {
    Expression left = exp.getLeft();
    Expression right = exp.getRight();

    if (left instanceof BinaryExpr) {
      return parseUrlFromBinaryExp((BinaryExpr) left);
    } else if (left instanceof StringLiteralExpr) {
      return formatURL((StringLiteralExpr) left);
    }

    // Check if right side is a binary expression
    if (right instanceof BinaryExpr) {
      return parseUrlFromBinaryExp((BinaryExpr) right);
    } else if (right instanceof StringLiteralExpr) {
      return formatURL((StringLiteralExpr) right);
    }

    return ""; // URL not found in subtree
  }

  // TODO format to what? add comments please
  private static String formatURL(StringLiteralExpr stringLiteralExpr) {
    String str = stringLiteralExpr.toString();
    str = str.replace("http://", "");
    str = str.replace("https://", "");

    int backslashNdx = str.indexOf("/");
    if (backslashNdx > 0) {
      str = str.substring(backslashNdx);
    }

    int questionNdx = str.indexOf("?");
    if (questionNdx > 0) {
      str = str.substring(0, questionNdx);
    }

    if (str.endsWith("\"")) {
      str = str.substring(0, str.length() - 1);
    }

    if (str.endsWith("/")) {
      str = str.substring(0, str.length() - 1);
    }

    return str;
  }

  /**
   * Get the HTTP method for the JSF exchange() method call.
   *
   * @param arguments the arguments of the exchange() method
   * @return the HTTP method extracted
   */
  private static String getHttpMethodForExchange(String arguments) {
    if (arguments.contains("HttpMethod.POST")) {
      return "POST";
    } else if (arguments.contains("HttpMethod.PUT")) {
      return "PUT";
    } else if (arguments.contains("HttpMethod.DELETE")) {
      return "DELETE";
    } else {
      return "GET"; // default
    }
  }

  public static JsonArray convertListToJsonArray(List<JsonObject> jsonObjectList) {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (JsonObject jsonObject : jsonObjectList) {
      arrayBuilder.add(jsonObject);
    }
    return arrayBuilder.build();
  }
}
