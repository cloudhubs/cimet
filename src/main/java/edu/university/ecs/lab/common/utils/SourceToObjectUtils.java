package edu.university.ecs.lab.common.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.enums.RestCallTemplate;
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
    private static File sourceFile;
    private static CompilationUnit cu;
    private static String microserviceName;
    private static String packageName;
    private static String packageAndClassName;
    private static CombinedTypeSolver combinedTypeSolver;
    static {
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver("C:\\Users\\ninja\\IdeaProjects\\cimet2\\clone\\train-ticket");

        combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        combinedTypeSolver.add(javaParserTypeSolver);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }


    private static void generateStaticValues(File sourceFile) {
        // Parse the highest level node being compilation unit
        try {
            cu = StaticJavaParser.parse(sourceFile);
        } catch (FileNotFoundException e) {
            Error.reportAndExit(Error.JPARSE_FAILED);
        }
        microserviceName = getMicroserviceName(sourceFile);
        if(!cu.findAll(PackageDeclaration.class).isEmpty()) {
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
        SourceToObjectUtils.sourceFile = sourceFile;
        generateStaticValues(sourceFile);

        // Calculate early to determine classrole based on annotation, filter for class based annotations only
        Set<Annotation> classAnnotations = parseAnnotations(cu.findAll(AnnotationExpr.class).stream().filter(annotationExpr -> {
            if (annotationExpr.getParentNode().isPresent()) {
                Node n = annotationExpr.getParentNode().get();
                return n instanceof ClassOrInterfaceDeclaration;
            }
            return false;
        }).collect(Collectors.toUnmodifiableList()));

        // calculate the preEndpointURL from RequestMapping annotation
        String preURL = classAnnotations.stream().filter(ae -> ae.getName().equals("RequestMapping")).map(Annotation::getContents).findFirst().orElse("");
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
            parseMethods(preURL, cu.findAll(MethodDeclaration.class)),
            parseFields(cu.findAll(FieldDeclaration.class)),
            classAnnotations,
            parseMethodCalls(cu.findAll(MethodDeclaration.class)),
            cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getImplementedTypes().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet()));

    }


    /**
     * This method parses methodDeclarations list and returns a Set of Method models
     *
     * @param preURL the preURL
     * @param methodDeclarations the list of methodDeclarations to be parsed
     * @return a set of Method models representing the MethodDeclarations
     */
    public static Set<Method> parseMethods(String preURL, List<MethodDeclaration> methodDeclarations) {
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

            method = convertValidEndpoints(preURL, methodDeclaration, method);


            methods.add(method);
        }

        return methods;
    }

    /**
     * This method converts a valid Method to an Endpoint
     *
     * @param preURL the preURL
     * @param methodDeclaration the MethodDeclaration associated with Method
     * @param method the Method to be converted
     * @return returns method if it is invalid, otherwise a new Endpoint
     */
    public static Method convertValidEndpoints(String preURL, MethodDeclaration methodDeclaration, Method method) {
        String url = preURL + getPathFromAnnotations(methodDeclaration.getAnnotations());
        if (method.getAnnotations().isEmpty() || url.isEmpty()) {
            return method;
        }
        HttpMethod httpMethod = HttpMethod.NONE;
        for (AnnotationExpr ae : methodDeclaration.getAnnotations()) {

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
                case "RequestMapping":
                    httpMethod = HttpMethod.GET;
                    if(ae instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr nae = (NormalAnnotationExpr) ae;
                        for(MemberValuePair mvp : nae.getPairs()) {
                            if(mvp.getNameAsString().equals("method")) {
                                switch (mvp.getValue().toString()) {
                                    case "RequestMethod.DELETE":
                                        httpMethod = HttpMethod.DELETE;
                                        break;
                                    case "RequestMethod.PUT":
                                        httpMethod = HttpMethod.PUT;
                                        break;
                                    case "RequestMethod.GET":
                                        httpMethod = HttpMethod.GET;
                                        break;
                                    case "RequestMethod.PATCH":
                                        httpMethod = HttpMethod.PATCH;
                                        break;
                                    case "RequestMethod.POST":
                                        httpMethod = HttpMethod.POST;
                                }
                            }
                        }
                    }
                    break;
            }
        }

        return new Endpoint(method, url, httpMethod, microserviceName);
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
                String calledServiceType = getCallingObjectType(mce);

                String parameterContents = mce.getArguments().stream().map(Objects::toString).collect(Collectors.joining(","));

                if (Objects.nonNull(calledServiceName)) {
                    MethodCall methodCall = new MethodCall(methodName, packageAndClassName, calledServiceType, calledServiceName, methodDeclaration.getNameAsString(), parameterContents);

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
     * @param methodCall the MethodCall to be converted
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
        } else if(methodCall.getParameterContents().contains("HttpMethod.PATCH")) {
            httpMethod = HttpMethod.PATCH;
        }

        if(httpMethod.equals(HttpMethod.NONE)) {
            httpMethod = RestCallTemplate.findHttpMethodByName(methodCall.getName());
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
     * This method gets url path from a list of annotation expressions
     *
     * @param annotationExprs the annotation expressions to parse
     * @return the string url
     */
    private static String getPathFromAnnotations(List<AnnotationExpr> annotationExprs) {
        for (AnnotationExpr ae : annotationExprs) {
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
                case "RequestMapping":
                    httpMethod = HttpMethod.GET;
                    if(ae instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr nae = (NormalAnnotationExpr) ae;
                        for(MemberValuePair mvp : nae.getPairs()) {
                            if(mvp.getNameAsString().equals("method")) {
                                switch (mvp.getValue().toString()) {
                                    case "RequestMethod.DELETE":
                                        httpMethod = HttpMethod.DELETE;
                                        break;
                                    case "RequestMethod.PUT":
                                        httpMethod = HttpMethod.PUT;
                                        break;
                                    case "RequestMethod.GET":
                                        httpMethod = HttpMethod.GET;
                                        break;
                                    case "RequestMethod.PATCH":
                                        httpMethod = HttpMethod.PATCH;
                                        break;
                                    case "RequestMethod.POST":
                                        httpMethod = HttpMethod.POST;
                                }
                            }
                        }
                    }
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
     * @return the name of the object the method is being called from
     */
    private static String getCallingObjectName(MethodCallExpr mce) {


        Expression scope = mce.getScope().orElse(null);

        if (Objects.nonNull(scope) && scope instanceof NameExpr) {
            NameExpr fae = scope.asNameExpr();
            return fae.getNameAsString();
        }

        return "";

    }

    private static String getCallingObjectType(MethodCallExpr mce) {

        Expression scope = mce.getScope().orElse(null);

        if (Objects.isNull(scope)) {
            return "";
        }

        try {
            // Resolve the type of the object
            var resolvedType = JavaParserFacade.get(combinedTypeSolver).getType(scope);
            List<String> parts = List.of(((ReferenceTypeImpl) resolvedType).getQualifiedName().split("\\."));
            if(parts.isEmpty()) {
                return "";
            }

            return parts.get(parts.size() - 1);
        } catch (Exception e) {
            if(e instanceof UnsolvedSymbolException && ((UnsolvedSymbolException) e).getName() != null) {
                return ((UnsolvedSymbolException) e).getName();
            }
            return "";
        }
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
            return formatURL(StringParserUtils.removeOuterQuotations(exp.toString()));
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
            returnString.append(formatURL(left.toString()));
        } else if (left instanceof NameExpr
                && !left.asNameExpr().getNameAsString().contains("url")
                && !left.asNameExpr().getNameAsString().contains("uri")) {
            returnString.append("/{?}");
        }

        // Check if right side is a binary expression
        if (right instanceof BinaryExpr) {
            returnString.append(parseUrlFromBinaryExp((BinaryExpr) right));
        } else if (right instanceof StringLiteralExpr) {
            returnString.append(formatURL(right.toString()));
        } else if (right instanceof NameExpr) {
            returnString.append("/{?}");
        }

        return returnString.toString(); // URL not found in subtree
    }

    private static String formatURL(String str) {
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
     * This method searches a set of Annotation models and returns a ClassRole found
     *
     * @param annotations the set of annotations to search
     * @return the ClassRole determined
     */
    private static ClassRole parseClassRole(Set<Annotation> annotations) {
        ClassRole classRole = ClassRole.UNKNOWN;
        for (Annotation annotation : annotations) {
            switch (annotation.getName()) {
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

    //TODO Generalize and move out
    private static String getMicroserviceName(File sourceFile) {
        return sourceFile.getPath().split(FileUtils.SEPARATOR_SPECIAL)[3];
    }
}
