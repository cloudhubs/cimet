package edu.university.ecs.lab.common.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;
import edu.university.ecs.lab.common.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/** Static utility class for parsing a file and returning associated models from code structure. */
public class SourceToObjectUtils {
  private static CompilationUnit cu;

  /**
   * Parse a Java class file and return a JClass object. The class role will be determined by {@link
   * ClassRole#fromSourceFile(File)} and the returned object will be of correct {@link JClass}
   * subclass type where applicable.
   *
   * @param sourceFile the file to parse
   * @return the JClass object representing the file
   * @throws IOException on parse error
   */
  public static JClass parseClass(File sourceFile, Config config) {

    // Parse the highest level node being compilation unit
    try {
      cu = StaticJavaParser.parse(sourceFile);
    } catch (FileNotFoundException e) {
      Error.reportAndExit(Error.JPARSE_FAILED);
    }

    // Calculate early to determine classrole based on annotation, filter for class based only
    Set<Annotation> classAnnotations = parseAnnotations(cu.findAll(AnnotationExpr.class).stream().filter(annotationExpr -> {
      if(annotationExpr.getParentNode().isPresent()) {
        Node n = annotationExpr.getParentNode().get();
        if(n instanceof ClassOrInterfaceDeclaration) {
          return true;
        }
      }
      return false;
    }).collect(Collectors.toUnmodifiableList()));

    String preURL = classAnnotations.stream().filter(ae -> ae.getAnnotationName().equals("RequestMapping")).map(Annotation::getContents).findFirst().orElse("");
    preURL = preURL.replace("\"", "");
    // Null returned if not needed class and caller will skip null JClasses
    ClassRole classRole = parseClassRole(classAnnotations);
    if(classRole.equals(ClassRole.UNKNOWN)) {
      return null;
    }

    JClass jClass =
            new JClass(
                    sourceFile.getName(),
                    sourceFile.getPath(),
                    cu.findAll(PackageDeclaration.class).get(0).getNameAsString(),
                    classRole,
                    parseMethods(getMicroserviceName(sourceFile), preURL,  cu.findAll(MethodDeclaration.class)),
                    parseFields(cu.findAll(FieldDeclaration.class)),
                    classAnnotations,
                    parseMethodCalls(getMicroserviceName(sourceFile), cu.findAll(MethodDeclaration.class)));

    return jClass;
  }


  public static Set<Method> parseMethods(String microserviceName, String preURL, List<MethodDeclaration> methodDeclarations) {
    // Get params and returnType
    Set<Method> methods = new HashSet<>();


    for(MethodDeclaration methodDeclaration : methodDeclarations) {
      Set<Field> parameters = new HashSet<>();
      for (Parameter parameter : methodDeclaration.getParameters()) {
        parameters.add(new Field(parameter.getNameAsString(), parameter.getTypeAsString()));
      }

      Method method = new Method(
              methodDeclaration.getNameAsString(),
              parameters,
              methodDeclaration.getTypeAsString(),
              parseAnnotations(methodDeclaration.getAnnotations()));

      method = convertValidEndpoints(microserviceName, preURL, methodDeclaration, method);


      methods.add(method);
    }

    return methods;
  }

  public static Method convertValidEndpoints(String microserviceName, String preURL, MethodDeclaration methodDeclaration, Method method) {
    String url = preURL + getPathFromAnnotations(methodDeclaration.getAnnotations());
    if(method.getAnnotations().isEmpty() || url.isEmpty()) {
      return method;
    }
    HttpMethod httpMethod = HttpMethod.NONE;
    for(AnnotationExpr ae : methodDeclaration.getAnnotations()) {

      switch (ae.getNameAsString()) {
        case "GetMapping":
          httpMethod = HttpMethod.GET;
          break;
        case "PostMapping":
          httpMethod = HttpMethod.POST;
          break;
        case "DeleteMapping":
          httpMethod = HttpMethod.DELETE;
          break;
        case "PutMapping":
          httpMethod = HttpMethod.PUT;
          break;
      }
    }

    return new Endpoint(method, url, httpMethod, microserviceName);
  }

  public static Set<MethodCall> parseMethodCalls(String microserviceName, List<MethodDeclaration> methodDeclarations) {
    Set<MethodCall> methodCalls = new HashSet<>();

    // loop through method calls
    for(MethodDeclaration methodDeclaration : methodDeclarations) {
      for (MethodCallExpr mce : methodDeclaration.findAll(MethodCallExpr.class)) {
        String methodName = mce.getNameAsString();

        String calledServiceName = getCallingObjectName(mce);
        String parameterContents = mce.getArguments().stream().map(Objects::toString).collect(Collectors.joining(","));

        if (Objects.nonNull(calledServiceName)) {
          MethodCall methodCall = new MethodCall(methodName, calledServiceName, methodDeclaration.getNameAsString(), parameterContents);

          methodCall = convertValidRestCalls(microserviceName, mce, methodCall);

          methodCalls.add(methodCall);
        }
      }
    }

    return methodCalls;
  }

  public static MethodCall convertValidRestCalls(String microserviceName, MethodCallExpr methodCallExpr, MethodCall methodCall) {
    if(!methodCall.getObjectName().equals("restTemplate")) {
      return methodCall;
    }
    String url = parseURL(methodCallExpr);
    if(url.isEmpty()) {
      return methodCall;
    }

    HttpMethod httpMethod = HttpMethod.NONE;
    if(methodCall.getParameterContents().contains("HttpMethod.GET")) {
      httpMethod = HttpMethod.GET;
    } else if(methodCall.getParameterContents().contains("HttpMethod.POST")) {
      httpMethod = HttpMethod.POST;
    } else if(methodCall.getParameterContents().contains("HttpMethod.DELETE")) {
      httpMethod = HttpMethod.DELETE;
    } else if(methodCall.getParameterContents().contains("HttpMethod.PUT")) {
      httpMethod = HttpMethod.PUT;
    }

    return new RestCall(methodCall, url, httpMethod, microserviceName);
  }

  private static Set<Field> parseFields(List<FieldDeclaration> fieldDeclarations) {
    Set<Field> javaFields = new HashSet<>();

    // loop through class declarations
    for (FieldDeclaration fd : fieldDeclarations) {
      for (VariableDeclarator variable : fd.getVariables()) {
        javaFields.add(new Field(variable.getTypeAsString(), variable.getNameAsString()));
      }

    }

    return javaFields;
  }

  private static String getPathFromAnnotations(List<AnnotationExpr> annotationExprs) {
    for(AnnotationExpr ae : annotationExprs) {
      HttpMethod httpMethod = HttpMethod.NONE;
      switch (ae.getNameAsString()) {
        case "GetMapping":
          httpMethod = HttpMethod.GET;
          break;
        case "PostMapping":
          httpMethod = HttpMethod.POST;
          break;
        case "DeleteMapping":
          httpMethod = HttpMethod.DELETE;
          break;
        case "PutMapping":
          httpMethod = HttpMethod.PUT;
          break;
      }


      if (ae.isSingleMemberAnnotationExpr() && httpMethod != HttpMethod.NONE) {
        return StringParserUtils.simplifyEndpointURL(
                StringParserUtils.removeOuterQuotations(
                        ae.asSingleMemberAnnotationExpr().getMemberValue().toString()));
      }

      if (ae.isNormalAnnotationExpr() && ae.asNormalAnnotationExpr().getPairs().size() > 0 && httpMethod != HttpMethod.NONE) {
        for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
          if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
            return StringParserUtils.simplifyEndpointURL(
                    StringParserUtils.removeOuterQuotations(mvp.getValue().toString()));
          }
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
  private static String getCallingObjectName(MethodCallExpr mce) {
    Expression scope = mce.getScope().orElse(null);

    if(Objects.isNull(scope)) {
      return "";
    }

    String calledServiceID = null;
    if (scope instanceof NameExpr) {
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
  private static String parseURL(MethodCallExpr mce) {
    if (mce.getArguments().isEmpty()) {
      return "";
    }

    // Arbitrary index of the url parameter
    Expression exp = mce.getArguments().get(0);

    if (exp.isStringLiteralExpr()) {
      return StringParserUtils.removeOuterQuotations(exp.toString());
    } else if (exp.isFieldAccessExpr()) {
      return parseFieldValue(exp.asFieldAccessExpr().getNameAsString());
    } else if (exp.isNameExpr()) {
      return parseFieldValue(exp.asNameExpr().getNameAsString());
    } else if (exp.isBinaryExpr()) {
      return parseUrlFromBinaryExp(exp.asBinaryExpr());
    }

    return "";
  }

  private static String parseFieldValue(String fieldName) {
    for (FieldDeclaration fd : cu.findAll(FieldDeclaration.class)) {
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



  private static Set<Annotation> parseAnnotations(List<AnnotationExpr> annotationExprs) {
    Set<Annotation> annotations = new HashSet<>();

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

  private static ClassRole parseClassRole(Set<Annotation> annotations) {
    ClassRole classRole = ClassRole.UNKNOWN;
    for(Annotation annotation : annotations) {
      switch (annotation.getAnnotationName()) {
        case "RestController":
          classRole = ClassRole.CONTROLLER;
          break;
        case "Service":
          classRole = ClassRole.SERVICE;
          break;
        case "Repository":
          classRole = ClassRole.REPOSITORY;
          break;
        case "Entity":
          classRole = ClassRole.ENTITY;
          break;
      }
    }

    return classRole;
  }

  private static String getMicroserviceName(File sourceFile) {
    return sourceFile.getPath().split("\\\\")[3];
  }
}
