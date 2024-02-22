package edu.university.ecs.lab.intermediate.services;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import edu.university.ecs.lab.common.models.RestDependency;
import edu.university.ecs.lab.common.models.RestCallMethod;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing REST dependencies from source files and describing them in relation to the
 * microservice that calls the endpoint.
 */
public class CallExtractionService {
  /**
   * Parse the REST dependencies from the given source file.
   *
   * @param sourceFile the source file to parse
   * @return the list of parsed dependencies
   * @throws IOException if an I/O error occurs
   */
  public List<RestDependency> parseCalls(File sourceFile) throws IOException {
    List<RestDependency> dependencies = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // don't analyze further if no RestTemplate import exists
    if (!hasRestTemplateImport(cu)) {
      return dependencies;
    }

    String packageName = StringParserUtils.findPackage(cu);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = cid.getNameAsString();

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        String methodName = md.getNameAsString();

        // loop through method calls
        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
          String methodCall = mce.getNameAsString();

          RestCallMethod restTemplateMethod = RestCallMethod.findByName(methodCall);

          if (restTemplateMethod != null) {
            Expression scope = mce.getScope().orElse(null);

            // match field access
            if (isRestTemplateScope(scope, cid)) {
              // construct rest call
              RestDependency restDependency = new RestDependency();
              restDependency.setSourceFile(sourceFile.getCanonicalPath());
              restDependency.setParentMethod(packageName + "." + className + "." + methodName);
              restDependency.setHttpMethod(restTemplateMethod.getHttpMethod().toString());

              // get http methods for exchange method
              if (restTemplateMethod.getMethodName().equals("exchange")) {
                restDependency.setHttpMethod(
                    getHttpMethodForExchange(mce.getArguments().toString()));
              }

              // find url
              restDependency.setUrl(findUrl(mce, cid));

              // skip empty urls
              if (restDependency.getUrl().equals("")) {
                continue;
              }

              // add to list of restCall
              dependencies.add(restDependency);
            }
          }
        }
      }
    }

    return dependencies;
  }

  /**
   * Get the HTTP method for the JSF exchange() method call.
   *
   * @param arguments the arguments of the exchange() method
   * @return the HTTP method extracted
   */
  private String getHttpMethodForExchange(String arguments) {
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

  /**
   * Find the URL from the given method call expression.
   *
   * @param mce the method call to extract url from
   * @param cid the class or interface to search
   * @return the URL found
   */
  private String findUrl(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
    if (mce.getArguments().isEmpty()) {
      return "";
    }

    Expression exp = mce.getArguments().get(0);

    if (exp.isStringLiteralExpr()) {
      return StringParserUtils.removeOuterQuotations(exp.toString());
    } else if (exp.isFieldAccessExpr()) {
      return fieldValue(cid, exp.asFieldAccessExpr().getNameAsString());
    } else if (exp.isNameExpr()) {
      return fieldValue(cid, exp.asNameExpr().getNameAsString());
    } else if (exp.isBinaryExpr()) {
      return resolveUrlFromBinaryExp(exp.asBinaryExpr());
    }

    return "";
  }

  /**
   * Check if the given compilation unit has a RestTemplate import in order to determine if it would
   * have any dependencies in the file.
   *
   * @param cu the compilation unit to check
   * @return if a RestTemplate import exists else false
   */
  private boolean hasRestTemplateImport(CompilationUnit cu) {
    for (ImportDeclaration id : cu.findAll(ImportDeclaration.class)) {
      if (id.getNameAsString().equals("org.springframework.web.client.RestTemplate")) {
        return true;
      }
    }
    return false;
  }

  private boolean isRestTemplateScope(Expression scope, ClassOrInterfaceDeclaration cid) {
    if (scope == null) {
      return false;
    }

    // field access: this.restTemplate
    if (scope.isFieldAccessExpr()
        && isRestTemplateField(cid, scope.asFieldAccessExpr().getNameAsString())) {
      return true;
    }

    // filed access without this
    if (scope.isNameExpr() && isRestTemplateField(cid, scope.asNameExpr().getNameAsString())) {
      return true;
    }

    return false;
  }

  private boolean isRestTemplateField(ClassOrInterfaceDeclaration cid, String fieldName) {
    for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
      if (fd.getElementType().toString().equals("RestTemplate")
          && fd.getVariables().toString().contains(fieldName)) {

        return true;
      }
    }
    return false;
  }

  private String fieldValue(ClassOrInterfaceDeclaration cid, String fieldName) {
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
  private String resolveUrlFromBinaryExp(BinaryExpr exp) {
    String url = "";

    String right = exp.getRight().toString();
    String left = exp.getLeft().toString();

    if (right.contains("/")) {
      url = right.substring(right.indexOf('/'));

      if (url.endsWith("\"")) {
        url = url.substring(0, url.length() - 1);
      }
    }

    if (left.contains("/")) {
      url += left.substring(left.indexOf('/'));

      if (url.endsWith("\"")) {
        url = url.substring(0, url.length() - 1);
      }
    }

    return url;
  }
}
