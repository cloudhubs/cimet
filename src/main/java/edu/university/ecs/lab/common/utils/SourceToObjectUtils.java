package edu.university.ecs.lab.common.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Static utility class for parsing a file and returning associated models from code structure.
 */
public class SourceToObjectUtils {
    private static final List<String> call_annotations = Arrays.asList("RequestMapping", "GetMapping", "PutMapping",
            "PostMapping", "DeleteMapping", "PatchMapping");
    private static CompilationUnit cu;
    private static String microserviceName;
    private static String packageName;
    private static String packageAndClassName;


    private static void generateStaticValues(File sourceFile) {
        // Parse the highest level node being compilation unit
        try {
            cu = StaticJavaParser.parse(sourceFile);
        } catch (FileNotFoundException e) {
            Error.reportAndExit(Error.JPARSE_FAILED);
        }
        microserviceName = getMicroserviceName(sourceFile);
        if (!cu.findAll(PackageDeclaration.class).isEmpty()) {
            packageName = cu.findAll(PackageDeclaration.class).get(0).getNameAsString();
            packageAndClassName = packageName + "." + sourceFile.getName().replace(".java", "");
        }

    }

    /**
     * This method parses a Java class file and return a JClass object.
     *
     * @param sourceFile the file to parse
     * @return the JClass object representing the file
     */
    public static JClass parseClass(File sourceFile, Config config) {
        generateStaticValues(sourceFile);

        // Calculate early to determine classrole based on annotation, filter for class based annotations only
        String preURL = "";
        HttpMethod preMethod = HttpMethod.NONE;
        List<AnnotationExpr> classAnnotations = new ArrayList<>();
        for (var ae: cu.findAll(AnnotationExpr.class)){
            if (ae.getParentNode().isPresent()) {
                Node n = ae.getParentNode().get();
                if (n instanceof ClassOrInterfaceDeclaration)
                {
                    classAnnotations.add(ae);
                    if (call_annotations.contains(ae.getNameAsString())) {
                        if (preURL.isEmpty()) {
                            preURL = getPathFromAnnotation(ae, "");
                        }
                        if (preMethod.equals(HttpMethod.NONE)) {
                            preMethod = getHttpMethodFromAnnotation(ae, preMethod);
                        }

                    }
                }
            }
        }
        preURL = preURL.replace("\"", "");

        ClassRole classRole = parseClassRole(classAnnotations);
        // Return unknown classRoles where annotation not found
        if (classRole.equals(ClassRole.UNKNOWN)) {
            return null;
        }

        // Build the JClass
        return new JClass(
                sourceFile.getName().replace(".java", ""),
                FileUtils.localPathToGitPath(sourceFile.getPath(), config.getRepoName()),
                packageName,
                classRole,
                parseMethods(cu.findAll(MethodDeclaration.class), preURL, preMethod),
                parseFields(cu.findAll(FieldDeclaration.class)),
                parseAnnotations(classAnnotations),
                parseMethodCalls(cu.findAll(MethodDeclaration.class)),
                cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getImplementedTypes().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet()));

    }


    /**
     * This method parses methodDeclarations list and returns a Set of Method models
     *
     * @param methodDeclarations the list of methodDeclarations to be parsed
     * @param preURL            initial part of the URL in case of recursion
     * @param preMethod         pre-defined HTTP method in case of recursion
     * @return a set of Method models representing the MethodDeclarations
     */
    public static Set<Method> parseMethods(List<MethodDeclaration> methodDeclarations, String preURL, HttpMethod preMethod) {
        // Get params and returnType
        Set<Method> methods = new HashSet<>();


        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            Set<Field> parameters = new HashSet<>();
            for (Parameter parameter : methodDeclaration.getParameters()) {
                parameters.add(new Field(parameter.getNameAsString(), packageAndClassName, parameter.getTypeAsString()));
            }

            Method method = new Method(
                    methodDeclaration.getNameAsString(),
                    packageAndClassName,
                    parameters,
                    methodDeclaration.getTypeAsString(),
                    parseAnnotations(methodDeclaration.getAnnotations()));

            method = convertValidEndpoints(methodDeclaration, method, preURL, preMethod);


            methods.add(method);
        }

        return methods;
    }

    /**
     * This method converts a valid Method to an Endpoint
     *
     * @param methodDeclaration the MethodDeclaration associated with Method
     * @param method            the Method to be converted
     * @param preURL            initial part of the URL in case of recursion
     * @param preMethod         pre-defined HTTP method in case of recursion
     * @return returns method if it is invalid, otherwise a new Endpoint
     */
    public static Method convertValidEndpoints(MethodDeclaration methodDeclaration, Method method, String preURL, HttpMethod preMethod) {
        HttpMethod httpMethod = HttpMethod.NONE;
        for (AnnotationExpr ae : methodDeclaration.getAnnotations()) {
            String ae_name = ae.getNameAsString();
            if (call_annotations.contains(ae_name)) {
                String url = getPathFromAnnotation(ae, preURL);
                if (preMethod.equals(HttpMethod.NONE)) {
                    httpMethod = getHttpMethodFromAnnotation(ae, httpMethod);
                }
                // By Spring documentation, only the first valid @Mapping annotation is considered;
                // And getAnnotations() return them in order, so we can return immediately
                return new Endpoint(method, url, httpMethod, microserviceName);
            }

        }
        return method;
    }

    private static HttpMethod getHttpMethodFromAnnotation(AnnotationExpr ae, HttpMethod httpMethod) {
        switch (ae.getNameAsString()) {
            case "GetMapping":
                return HttpMethod.GET;
            case "PostMapping":
                return HttpMethod.POST;
            case "DeleteMapping":
                return HttpMethod.DELETE;
            case "PutMapping":
                return HttpMethod.PUT;
            case "PatchMapping":
                return HttpMethod.PATCH;
            case "RequestMapping":
                if (ae instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr nae = (NormalAnnotationExpr) ae;
                    if (nae.getPairs().isEmpty()) {
                        // This is a RequestMapping without parameters
                        return HttpMethod.NONE; // or set a default method, if you prefer
                    } else {
                        for (MemberValuePair pair : nae.getPairs()) {
                            if (pair.getNameAsString().equals("method")) {
                                String methodValue = pair.getValue().toString();
                                switch (methodValue) {
                                    case "RequestMethod.GET":
                                        return HttpMethod.GET;
                                    case "RequestMethod.POST":
                                        return HttpMethod.POST;
                                    case "RequestMethod.DELETE":
                                        return HttpMethod.DELETE;
                                    case "RequestMethod.PUT":
                                        return HttpMethod.PUT;
                                }
                            }
                        }
                    }
                }
            default:
                return httpMethod;
        }
    }

    private static String getPathFromAnnotation(AnnotationExpr ae, String url) {
        // Annotations of type @Mapping("/endpoint")
        if (ae.isSingleMemberAnnotationExpr()) {
            url = url + StringParserUtils.simplifyEndpointURL(
                    StringParserUtils.removeOuterQuotations(
                            ae.asSingleMemberAnnotationExpr().getMemberValue().toString()));
        }

        // Annotations of type @Mapping(path="/endpoint")
        else if (ae.isNormalAnnotationExpr() && !ae.asNormalAnnotationExpr().getPairs().isEmpty()) {
            for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
                if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
                    url = url + StringParserUtils.simplifyEndpointURL(
                            StringParserUtils.removeOuterQuotations(mvp.getValue().toString()));
                    break;
                }
            }
        }
        return url;
    }

    /**
     * This method parses methodDeclarations list and returns a Set of MethodCall models
     *
     * @param methodDeclarations the list of methodDeclarations to be parsed
     * @return a set of MethodCall models representing MethodCallExpressions found in the MethodDeclarations
     */
    public static Set<MethodCall> parseMethodCalls(List<MethodDeclaration> methodDeclarations) {
        Set<MethodCall> methodCalls = new HashSet<>();

        // loop through method calls
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            for (MethodCallExpr mce : methodDeclaration.findAll(MethodCallExpr.class)) {
                String methodName = mce.getNameAsString();

                String calledServiceName = getCallingObjectName(mce);
                String parameterContents = mce.getArguments().stream().map(Objects::toString).collect(Collectors.joining(","));

                if (Objects.nonNull(calledServiceName)) {
                    MethodCall methodCall = new MethodCall(methodName, packageAndClassName, calledServiceName, methodDeclaration.getNameAsString(), parameterContents);

                    methodCall = convertValidRestCalls(mce, methodCall);

                    methodCalls.add(methodCall);
                }
            }
        }

        return methodCalls;
    }

    /**
     * This method converts a valid MethodCall to an RestCall
     *
     * @param methodCallExpr the MethodDeclaration associated with Method
     * @param methodCall     the MethodCall to be converted
     * @return returns methodCall if it is invalid, otherwise a new RestCall
     */
    public static MethodCall convertValidRestCalls(MethodCallExpr methodCallExpr, MethodCall methodCall) {
        if (!methodCall.getObjectName().equals("restTemplate")) {
            return methodCall;
        }
        String url = parseURL(methodCallExpr);
        if (url.isEmpty()) {
            return methodCall;
        }

        HttpMethod httpMethod = HttpMethod.NONE;
        if (methodCall.getParameterContents().contains("HttpMethod.GET")) {
            httpMethod = HttpMethod.GET;
        } else if (methodCall.getParameterContents().contains("HttpMethod.POST")) {
            httpMethod = HttpMethod.POST;
        } else if (methodCall.getParameterContents().contains("HttpMethod.DELETE")) {
            httpMethod = HttpMethod.DELETE;
        } else if (methodCall.getParameterContents().contains("HttpMethod.PUT")) {
            httpMethod = HttpMethod.PUT;
        }

        return new RestCall(methodCall, url, httpMethod, microserviceName);
    }

    /**
     * This method converts a list of FieldDeclarations to a set of Field models
     *
     * @param fieldDeclarations the field declarations to parse
     * @return the set of Field models
     */
    private static Set<Field> parseFields(List<FieldDeclaration> fieldDeclarations) {
        Set<Field> javaFields = new HashSet<>();

        // loop through class declarations
        for (FieldDeclaration fd : fieldDeclarations) {
            for (VariableDeclarator variable : fd.getVariables()) {
                javaFields.add(new Field(variable.getNameAsString(), packageAndClassName, variable.getTypeAsString()));
            }

        }

        return javaFields;
    }

    /**
     * Get the name of the object a method is being called from (callingObj.methodName())
     *
     * @return the name of the object the method is being called from
     */
    private static String getCallingObjectName(MethodCallExpr mce) {
        Expression scope = mce.getScope().orElse(null);

        if (Objects.isNull(scope)) {
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
     * @return the URL found
     */
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
     * This method parses a list of annotation expressions and returns a set of Annotation models
     *
     * @param annotationExprs the annotation expressions to parse
     * @return the Set of Annotation models
     */
    private static Set<Annotation> parseAnnotations(List<AnnotationExpr> annotationExprs) {
        Set<Annotation> annotations = new HashSet<>();

        for (AnnotationExpr ae : annotationExprs) {
            Annotation annotation;
            if (ae.isNormalAnnotationExpr()) {
                NormalAnnotationExpr normal = ae.asNormalAnnotationExpr();
                annotation = new Annotation(ae.getNameAsString(), packageAndClassName, normal.getPairs().toString());

            } else if (ae.isSingleMemberAnnotationExpr()) {
                annotation =
                        new Annotation(
                                ae.getNameAsString(),
                                packageAndClassName,
                                ae.asSingleMemberAnnotationExpr().getMemberValue().toString());
            } else {
                annotation = new Annotation(ae.getNameAsString(), packageAndClassName, "");
            }

            annotations.add(annotation);
        }

        return annotations;
    }

    /**
     * This method searches a list of Annotation expressions and returns a ClassRole found
     *
     * @param annotations the list of annotations to search
     * @return the ClassRole determined
     */
    private static ClassRole parseClassRole(List<AnnotationExpr> annotations) {
        for (AnnotationExpr annotation : annotations) {
            switch (annotation.getNameAsString()) {
                case "RestController":
                    return ClassRole.CONTROLLER;
                case "Service":
                    return ClassRole.SERVICE;
                case "Repository":
                    return ClassRole.REPOSITORY;
                case "Entity":
                    return ClassRole.ENTITY;
            }
        }
        return ClassRole.UNKNOWN;
    }

    //TODO Generalize and move out
    private static String getMicroserviceName(File sourceFile) {
        return sourceFile.getPath().split(FileUtils.SEPARATOR_SPECIAL)[3];
    }
}
