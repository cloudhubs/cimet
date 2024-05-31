package edu.university.ecs.lab.common.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.models.InputRepository;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.RestTemplate;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;
import edu.university.ecs.lab.common.models.*;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Static utility class for parsing a file and returning associated models from code structure. */
public class SourceToObjectUtils {

  /**
   * Parse a Java class file and return a JClass object. The class role will be determined by {@link
   * ClassRole#fromSourceFile(File)} and the returned object will be of correct {@link JClass}
   * subclass type where applicable.
   *
   * @param sourceFile the file to parse
   * @return the JClass object representing the file
   * @throws IOException on parse error
   */
  // TODO move this logic to JClass
  public static JClass parseClass(File sourceFile, Config config) throws IOException {
    CompilationUnit cu;
    try {
      cu = StaticJavaParser.parse(sourceFile);
    } catch (FileNotFoundException e) {
      return JClass.deletedClass(sourceFile, config);
    }

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return null;
    }

    String msId = getMicroserviceName(sourceFile, config);

    JClass jClass =
        new JClass(
            sourceFile.getName(),
            getRepositoryPath(sourceFile, config),
            packageName,
            ClassRole.fromSourceFile(sourceFile),
            parseMethods(cu),
            parseFields(cu),
            parseAnnotations(cu.getClassByName(sourceFile.getName().replace(".java", ""))),
            parseMethodCalls(cu, msId),
            msId);

    // Handle special class roles
//    if (jClass.getClassRole() == ClassRole.CONTROLLER) {
//      JController controller = new JController(jClass);
//      controller.setEndpoints(parseEndpoints(msId, sourceFile));
//      return controller;
//    } else if (jClass.getClassRole() == ClassRole.SERVICE) {
//      JService service = new JService(jClass);
//      service.setRestCalls(parseRestCalls(cu, msId));
//      return service;
//    }

    return jClass;
  }

  /**
   * Get the service name from the given file. This is determined by the file path and config.
   *
   * @param sourceFile the file to parse
   * @return the service name of the file, null if not found TODO this logic is now in {@link
   *     InputRepository#getServiceNameFromPath(String)}, refactor and delete
   */
  public static String getMicroserviceName(File sourceFile, Config config) {
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
              System.err.println(
                  "Failed to get service name from path \"" + filePath + "\": " + e.getMessage());
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Get the path from the repository TLD of the file from the clonePath directory. This will look
   * like repoName/serviceName/path/to/file.java
   *
   * @param sourceFile the file to get the path of
   * @param config system input config file
   * @return the relative path of the file after ./clonePath/ TODO this logic should be put in
   *     {@link InputRepository}, refactor and delete
   */
  public static String getRepositoryPath(File sourceFile, Config config) {
    // Get the file path start from the clonePath directory
    String filePath = sourceFile.getAbsolutePath();
    String clonePath = config.getClonePath();

    // Sanitize clonePath
    clonePath = clonePath.replace("./", "").replace(".\\", "");

    int clonePathIndex = filePath.indexOf(clonePath);

    if (clonePathIndex == -1) {
      System.err.println(
          "Error: File path does not contain clone path when trying to get relativePath: "
              + filePath);
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

//  public static List<Endpoint> parseEndpoints(String msId, File sourceFile) throws IOException {
//    List<Endpoint> endpoints = new ArrayList<>();
//
//    CompilationUnit cu = StaticJavaParser.parse(sourceFile);
//
//    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
//      AnnotationExpr aExpr = cid.getAnnotationByName("RequestMapping").orElse(null);
//
//      if (aExpr == null) {
//        return endpoints;
//      }
//
//      String classLevelPath = pathFromAnnotation(aExpr);
//
//      // loop through methods
//      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
//
//        // loop through annotations
//        for (AnnotationExpr ae : md.getAnnotations()) {
//          String url = StringParserUtils.mergePaths(classLevelPath, pathFromAnnotation(ae));
//          String decorator = ae.getNameAsString();
//          String httpMethod = null;
//          // TODO move logic to enum
//          switch (ae.getNameAsString()) {
//            case "GetMapping":
//              httpMethod = "GET";
//              break;
//            case "PostMapping":
//              httpMethod = "POST";
//              break;
//            case "DeleteMapping":
//              httpMethod = "DELETE";
//              break;
//            case "PutMapping":
//              httpMethod = "PUT";
//              break;
//            case "RequestMapping":
//              if (ae.toString().contains("RequestMethod.POST")) {
//                httpMethod = "POST";
//              } else if (ae.toString().contains("RequestMethod.DELETE")) {
//                httpMethod = "DELETE";
//              } else if (ae.toString().contains("RequestMethod.PUT")) {
//                httpMethod = "PUT";
//              } else {
//                httpMethod = "GET";
//              }
//              break;
//          }
//
//          if (httpMethod != null) {
//            endpoints.add(new Endpoint(parseMethod(md), url, decorator, httpMethod, msId));
//          }
//        }
//      }
//    }
//
//    return endpoints;
//  }

  public static Method parseMethod(MethodDeclaration md) {
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

    return new Method(
        md.getNameAsString(),
        parameter.toString(),
        md.getTypeAsString(),
        parseAnnotations(md.getAnnotations()));
  }

//  public static List<RestCall> parseRestCalls(CompilationUnit cu, String msId) throws IOException {
//    List<RestCall> restCalls = new ArrayList<>();
//
//    // loop through class declarations
//    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
//      // loop through methods
//
//      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
//        String calledFromMethodName = md.getNameAsString();
//
//        // loop through method calls
//        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
//          String methodName = mce.getNameAsString();
//          Expression scope = mce.getScope().orElse(null);
//
//          RestTemplate callTemplate = RestTemplate.findCallByName(methodName);
//          String calledServiceName = getCallingObjectName(scope);
//          String payloadObject = "";
//
//          HttpMethod httpMethod;
//          // Are we a rest call
//          if (!Objects.isNull(callTemplate)
//              && Objects.nonNull(calledServiceName)
//              && calledServiceName.equals("restTemplate")) {
//            // get http methods for exchange method
//            if (callTemplate.getMethodName().equals("exchange")) {
//              httpMethod = RestTemplate.getHttpMethodForExchange(mce.getArguments().toString());
//              // We are arbitrarily setting it, temporary
//              payloadObject =
//                  mce.getArguments().size() >= 2 ? mce.getArguments().get(2).toString() : "";
//            } else {
//              httpMethod = callTemplate.getHttpMethod();
//            }
//
//            // TODO find a more graceful way of handling/validating this can be passed up
//            if (parseURL(mce, cid).equals("")) {
//              continue;
//            }
//
//            RestCall call =
//                new RestCall(
//                    callTemplate.getMethodName(),
//                    calledServiceName,
//                    calledFromMethodName,
//                    msId,
//                    httpMethod,
//                    parseURL(mce, cid),
//                    "",
//                    "",
//                    payloadObject);
//            restCalls.add(call);
//          }
//        }
//      }
//    }
//    return restCalls;
//  }

  public static List<MethodCall> parseMethodCalls(CompilationUnit cu, String msId)
      throws IOException {
    List<MethodCall> methodCalls = new ArrayList<>();

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      // loop through methods

      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        String parentMethodName = md.getNameAsString();

        // loop through method calls
        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
          String methodName = mce.getNameAsString();
          Expression scope = mce.getScope().orElse(null);

          RestTemplate template = RestTemplate.findCallByName(methodName);
          String calledServiceName = getCallingObjectName(scope);

          // Are we a rest call
          if (!Objects.isNull(template)
              && Objects.nonNull(calledServiceName)
              && calledServiceName.equals("restTemplate")) {
            // do nothing, we only want regular methodCalls
            // System.out.println(restCall);
          } else if (Objects.nonNull(calledServiceName)) {
            methodCalls.add(
                new MethodCall(methodName, getCallingObjectName(scope), parentMethodName, msId));
          }
        }
      }
    }
    return methodCalls;
  }

  private static List<Field> parseFields(CompilationUnit cu) throws IOException {
    List<Field> javaFields = new ArrayList<>();

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

  private static String pathFromAnnotation(AnnotationExpr ae) {
    if (ae == null) {
      return "";
    }

    if (ae.isSingleMemberAnnotationExpr()) {
      return StringParserUtils.simplifyEndpointURL(
          StringParserUtils.removeOuterQuotations(
              ae.asSingleMemberAnnotationExpr().getMemberValue().toString()));
    }

    if (ae.isNormalAnnotationExpr() && ae.asNormalAnnotationExpr().getPairs().size() > 0) {
      for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
        if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
          return StringParserUtils.simplifyEndpointURL(
              StringParserUtils.removeOuterQuotations(mvp.getValue().toString()));
        }
      }
    }

    return "";
  }

  /**
   * Get the name of the object a method is being called from (callingObj.methodName())
   *
   * @param scope the scope to search
   * @return the name of the object the method is being called from
   */
  private static String getCallingObjectName(Expression scope) {
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
  // TODO: what is URL here? Is it the URL of the service? Or the URL of the method call? Rename to
  // avoid confusion
  private static String parseURL(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
    if (mce.getArguments().isEmpty()) {
      return "";
    }

    // Arbitrary index of the url parameter
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
    StringBuilder returnString = new StringBuilder();
    Expression left = exp.getLeft();
    Expression right = exp.getRight();

    if (left instanceof BinaryExpr) {
      returnString.append(parseUrlFromBinaryExp((BinaryExpr) left));
    } else if (left instanceof StringLiteralExpr) {
      returnString.append(formatURL((StringLiteralExpr) left));
    } else if (left instanceof NameExpr
        && !left.asNameExpr().getNameAsString().contains("url")
        && !left.asNameExpr().getNameAsString().contains("uri")) {
      returnString.append("/{?}");
    }

    // Check if right side is a binary expression
    if (right instanceof BinaryExpr) {
      returnString.append(parseUrlFromBinaryExp((BinaryExpr) right));
    } else if (right instanceof StringLiteralExpr) {
      returnString.append(formatURL((StringLiteralExpr) right));
    } else if (right instanceof NameExpr) {
      returnString.append("/{?}");
    }

    return returnString.toString(); // URL not found in subtree
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

  private static List<Annotation> parseAnnotations(Optional<ClassOrInterfaceDeclaration> cid) {
    if (cid.isEmpty()) {
      return new ArrayList<>();
    }

    return parseAnnotations(cid.get().getAnnotations());
  }

  private static List<Annotation> parseAnnotations(NodeList<AnnotationExpr> annotationExprs) {
    List<Annotation> annotations = new ArrayList<>();

    for (AnnotationExpr ae : annotationExprs) {
      Annotation annotation;
      if (ae.isNormalAnnotationExpr()) {
        NormalAnnotationExpr normal = ae.asNormalAnnotationExpr();
        annotation = new Annotation(ae.getNameAsString(), normal.getPairs().toString());

      } else if (ae.isSingleMemberAnnotationExpr()) {
        annotation =
            new Annotation(
                ae.getNameAsString(),
                ae.asSingleMemberAnnotationExpr().getMemberValue().toString());
      } else {
        annotation = new Annotation(ae.getNameAsString(), "");
      }

      annotations.add(annotation);
    }

    return annotations;
  }
}
