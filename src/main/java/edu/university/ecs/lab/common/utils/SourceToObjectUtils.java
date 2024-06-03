package edu.university.ecs.lab.common.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.RestTemplate;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;
import edu.university.ecs.lab.common.models.*;
import javassist.NotFoundException;
import javassist.compiler.ast.MethodDecl;

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
  public static JClass parseClass(File sourceFile, Config config) throws IOException {
    CompilationUnit cu = null;

    // Parse the highest level node being compilation unit
    try {
      cu = StaticJavaParser.parse(sourceFile);
    } catch (FileNotFoundException e) {
      Error.reportAndExit(Error.UNKNOWN_ERROR);
    }

    // Calculate early to determine classrole based on annotation
    List<Annotation> annotations = parseAnnotations(cu.findAll(AnnotationExpr.class));

    // Null returned if not needed class and caller will skip null JClasses
    ClassRole classRole = parseClassRole(annotations);
    if(classRole.equals(ClassRole.UNKNOWN)) {
      return null;
    }

    JClass jClass =
            new JClass(
                    sourceFile.getName(),
                    ConfigUtil.getGitRelativePath(sourceFile.getPath()),
                    cu.findAll(PackageDeclaration.class).get(0).getNameAsString(),
                    classRole,
                    parseMethods(cu.findAll(MethodDeclaration.class)),
                    parseFields(cu.findAll(FieldDeclaration.class)),
                    annotations,
                    parseMethodCalls(cu.findAll(MethodDeclaration.class)));

    return jClass;
  }


  public static List<Method> parseMethods(List<MethodDeclaration> methodDeclarations) {
    // Get params and returnType
    List<Method> methods = new ArrayList<>();


    for(MethodDeclaration methodDeclaration : methodDeclarations) {
      List<Field> parameters = new ArrayList<>();
      for (Parameter parameter : methodDeclaration.getParameters()) {
        parameters.add(new Field(parameter.getNameAsString(), parameter.getTypeAsString()));
      }

      methods.add(new Method(
              methodDeclaration.getNameAsString(),
              parameters,
              methodDeclaration.getTypeAsString(),
              parseAnnotations(methodDeclaration.getAnnotations())));
    }

    return methods;
  }

  public static List<MethodCall> parseMethodCalls(List<MethodDeclaration> methodDeclarations) {
    List<MethodCall> methodCalls = new ArrayList<>();

    // loop through method calls
    for(MethodDeclaration methodDeclaration : methodDeclarations) {
      for (MethodCallExpr mce : methodDeclaration.findAll(MethodCallExpr.class)) {
        String methodName = mce.getNameAsString();

        RestTemplate template = RestTemplate.findCallByName(methodName);
        String calledServiceName = getCallingObjectName(mce);

        // Are we a rest call
        if (!Objects.isNull(template)
                && Objects.nonNull(calledServiceName)
                && calledServiceName.equals("restTemplate")) {
          // do nothing, we only want regular methodCalls
          // System.out.println(restCall);
        } else if (Objects.nonNull(calledServiceName)) {
          methodCalls.add(
                  new MethodCall(methodName, calledServiceName, methodDeclaration.getNameAsString()));
        }
      }
    }

    return methodCalls;
  }

  private static List<Field> parseFields(List<FieldDeclaration> fieldDeclarations) {
    List<Field> javaFields = new ArrayList<>();

    // loop through class declarations
    for (FieldDeclaration fd : fieldDeclarations) {
      for (VariableDeclarator variable : fd.getVariables()) {
        javaFields.add(new Field(variable));
      }

    }

    return javaFields;
  }

//  private static String pathFromAnnotation(AnnotationExpr ae) {
//    if (ae == null) {
//      return "";
//    }
//
//    if (ae.isSingleMemberAnnotationExpr()) {
//      return StringParserUtils.simplifyEndpointURL(
//          StringParserUtils.removeOuterQuotations(
//              ae.asSingleMemberAnnotationExpr().getMemberValue().toString()));
//    }
//
//    if (ae.isNormalAnnotationExpr() && ae.asNormalAnnotationExpr().getPairs().size() > 0) {
//      for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
//        if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
//          return StringParserUtils.simplifyEndpointURL(
//              StringParserUtils.removeOuterQuotations(mvp.getValue().toString()));
//        }
//      }
//    }
//
//    return "";
//  }

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



  private static List<Annotation> parseAnnotations(List<AnnotationExpr> annotationExprs) {
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

  private static ClassRole parseClassRole(List<Annotation> annotations) {
    for(Annotation annotation : annotations) {
      switch (annotation.getAnnotationName()) {
        case "Controller":
          return ClassRole.CONTROLLER;
        case "Service":
          return ClassRole.SERVICE;
        case "Repository":
          return ClassRole.REPOSITORY;
        default:
          return ClassRole.UNKNOWN;
      }
    }

    return ClassRole.UNKNOWN;
  }
}
