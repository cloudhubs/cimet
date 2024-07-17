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
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.EndpointTemplate;
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
    private static final List<String> call_annotations = Arrays.asList("RequestMapping", "GetMapping", "PutMapping",
            "PostMapping", "DeleteMapping", "PatchMapping", "RepositoryRestResource", "FeignClient");
    private static CompilationUnit cu;
    private static String microserviceName;
    private static String path;
    private static String className;
    private static String packageName;
    private static String packageAndClassName;
    private static CombinedTypeSolver combinedTypeSolver;
    private static Config config;


    private static void generateStaticValues(File sourceFile, Config config1) {
        // Parse the highest level node being compilation unit
        config = config1;
        try {
            cu = StaticJavaParser.parse(sourceFile);
        } catch (Exception e) {
            Error.reportAndExit(Error.JPARSE_FAILED);
        }
        if (!cu.findAll(PackageDeclaration.class).isEmpty()) {
            packageName = cu.findAll(PackageDeclaration.class).get(0).getNameAsString();
            packageAndClassName = packageName + "." + sourceFile.getName().replace(".java", "");
        }
        path = FileUtils.localPathToGitPath(sourceFile.getPath(), config.getRepoName());

        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(FileUtils.getClonePath(config.getRepoName()));

        combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        combinedTypeSolver.add(javaParserTypeSolver);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        className = sourceFile.getName().replace(".java", "");

    }

    /**
     * This method parses a Java class file and return a JClass object.
     *
     * @param sourceFile the file to parse
     * @return the JClass object representing the file
     */
    public static JClass parseClass(File sourceFile, Config config, String microserviceName) {
        // Guard condition
        if(Objects.isNull(sourceFile) || FileUtils.isConfigurationFile(sourceFile.getPath())) {
            return null;
        }

        generateStaticValues(sourceFile, config);
        if (!microserviceName.isEmpty()) {
            SourceToObjectUtils.microserviceName = microserviceName;
        } else {
            SourceToObjectUtils.microserviceName = getMicroserviceName(sourceFile);
        }

        // Calculate early to determine classrole based on annotation, filter for class based annotations only
        List<AnnotationExpr> classAnnotations = filterClassAnnotations();
        AnnotationExpr requestMapping = classAnnotations.stream().filter(ae -> ae.getNameAsString().equals("RequestMapping")).findFirst().orElse(null);

        ClassRole classRole = parseClassRole(classAnnotations);

        // Return unknown classRoles where annotation not found
        if (classRole.equals(ClassRole.UNKNOWN)) {
            return null;
        }

        JClass jClass = null;
        if(classRole == ClassRole.FEIGN_CLIENT) {
            jClass = handleFeignClient(requestMapping, classAnnotations);
        } else {
            jClass = new JClass(
                    className,
                    path,
                    packageName,
                    classRole,
                    parseMethods(cu.findAll(MethodDeclaration.class), requestMapping),
                    parseFields(cu.findAll(FieldDeclaration.class)),
                    parseAnnotations(classAnnotations),
                    parseMethodCalls(cu.findAll(MethodDeclaration.class)),
                    cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getImplementedTypes().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet()));
        }

        // Build the JClass
        return jClass;

    }


    /**
     * This method parses methodDeclarations list and returns a Set of Method models
     *
     * @param methodDeclarations the list of methodDeclarations to be parsed
     * @return a set of Method models representing the MethodDeclarations
     */
    public static Set<Method> parseMethods(List<MethodDeclaration> methodDeclarations, AnnotationExpr requestMapping) {
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
                    parseAnnotations(methodDeclaration.getAnnotations()),
                    microserviceName,
                    className);

            method = convertValidEndpoints(methodDeclaration, method, requestMapping);

            methods.add(method);
        }

        return methods;
    }

    /**
     * This method converts a valid Method to an Endpoint
     *
     * @param methodDeclaration the MethodDeclaration associated with Method
     * @param method            the Method to be converted
     * @param requestMapping    the class level requestMapping
     * @return returns method if it is invalid, otherwise a new Endpoint
     */
    public static Method convertValidEndpoints(MethodDeclaration methodDeclaration, Method method, AnnotationExpr requestMapping) {
        for (AnnotationExpr ae : methodDeclaration.getAnnotations()) {
            String ae_name = ae.getNameAsString();
            if (EndpointTemplate.ENDPOINT_ANNOTATIONS.contains(ae_name)) {
                EndpointTemplate endpointTemplate = new EndpointTemplate(requestMapping, ae);

                // By Spring documentation, only the first valid @Mapping annotation is considered;
                // And getAnnotations() return them in order, so we can return immediately
                return new Endpoint(method, endpointTemplate.getUrl(), endpointTemplate.getHttpMethod());
            }
        }

        return method;
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
                    MethodCall methodCall = new MethodCall(methodName, packageAndClassName, calledServiceType, calledServiceName,
                            methodDeclaration.getNameAsString(), parameterContents, microserviceName, className);

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
        if (!methodCall.getObjectType().equals("RestTemplate") || !RestCallTemplate.REST_METHODS.contains(methodCallExpr.getNameAsString())) {
            return methodCall;
        }

        RestCallTemplate restCallTemplate = new RestCallTemplate(methodCallExpr, cu);

        if (restCallTemplate.getUrl().isEmpty()) {
            return methodCall;
        }

        return new RestCall(methodCall, restCallTemplate.getUrl(), restCallTemplate.getHttpMethod());
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
     * This method parses a list of annotation expressions and returns a set of Annotation models
     *
     * @param annotationExprs the annotation expressions to parse
     * @return the Set of Annotation models
     */
    private static List<Annotation> parseAnnotations(List<AnnotationExpr> annotationExprs) {
        List<Annotation> annotations = new ArrayList<>();

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
                case "Controller":
                    return ClassRole.CONTROLLER;
                case "Service":
                    return ClassRole.SERVICE;
                case "Repository":
                case "RepositoryRestResource":
                    return ClassRole.REPOSITORY;
                case "Entity":
                    return ClassRole.ENTITY;
                case "FeignClient":
                    return ClassRole.FEIGN_CLIENT;
            }
        }
        return ClassRole.UNKNOWN;
    }

    /**
     * Get the name of the microservice based on the file
     *
     * @param sourceFile the file we are getting microservice name for
     * @return
     */
    private static String getMicroserviceName(File sourceFile) {
        List<String> split = Arrays.asList(sourceFile.getPath().split(FileUtils.SEPARATOR_SPECIAL));
        return split.get(3);
    }

    /**
     * FeignClient represents an interface for making rest calls to a service
     * other than the current one. As such this method converts feignClient
     * interfaces into a service class whose methods simply contain the exact
     * rest call outlined by the interface annotations.
     *
     * @param classAnnotations
     * @return
     */
    private static JClass handleFeignClient(AnnotationExpr requestMapping, List<AnnotationExpr> classAnnotations) {

        // Parse the methods
        Set<Method> methods = parseMethods(cu.findAll(MethodDeclaration.class), requestMapping);

        // New methods for conversion
        Set<Method> newMethods = new HashSet<>();
        // New rest calls for conversion
        Set<MethodCall> newRestCalls = new HashSet<>();

        // For each method that is detected as an endpoint convert into a Method + RestCall
        for(Method method : methods) {
            if(method instanceof Endpoint) {
                Endpoint endpoint = (Endpoint) method;
                newMethods.add(new Method(method.getName(), packageAndClassName, method.getParameters(), method.getReturnType(), method.getAnnotations(), method.getMicroserviceName(), method.getClassName()));
                newRestCalls.add(new RestCall(new MethodCall("exchange", packageAndClassName, "RestCallTemplate", "restCallTemplate", method.getName(), "", endpoint.getMicroserviceName(), endpoint.getClassName()), endpoint.getUrl(), endpoint.getHttpMethod()));
            } else {
                newMethods.add(method);
            }
        }


        // Build the JClass
        return new JClass(
                className,
                path,
                packageName,
                ClassRole.FEIGN_CLIENT,
                newMethods,
                parseFields(cu.findAll(FieldDeclaration.class)),
                parseAnnotations(classAnnotations),
                newRestCalls,
                cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getImplementedTypes().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet()));
    }

    public static ConfigFile parseConfigurationFile(File file, Config config) {
        if(file.getName().endsWith(".yml")) {
            return NonJsonReadWriteUtils.readFromYaml(file.getPath(), config);
        } else if(file.getName().equals("DockerFile")) {
            return NonJsonReadWriteUtils.readFromDocker(file.getPath(), config);
        } else if(file.getName().equals("pom.xml")) {
            return NonJsonReadWriteUtils.readFromPom(file.getPath(), config);
        } else {
            return null;
        }
    }

    private static List<AnnotationExpr> filterClassAnnotations() {
        List<AnnotationExpr> classAnnotations = new ArrayList<>();
        for (AnnotationExpr ae : cu.findAll(AnnotationExpr.class)) {
            if (ae.getParentNode().isPresent()) {
                Node n = ae.getParentNode().get();
                if (n instanceof ClassOrInterfaceDeclaration) {
                    classAnnotations.add(ae);
                }
            }
        }
        return classAnnotations;
    }

    /**
     * FeignClient represents an interface for making rest calls to a service
     * other than the current one. As such this method converts feignClient
     * interfaces into a service class whose methods simply contain the exact
     * rest call outlined by the interface annotations.
     *
     * @param sourceFile
     * @param config
     * @param classAnnotations
     * @return
     */
    private static JClass handleRepositoryRestResource(File sourceFile, Config config, AnnotationExpr requestMapping, List<AnnotationExpr> classAnnotations) {

        // Parse the methods
        Set<Method> methods = parseMethods(cu.findAll(MethodDeclaration.class), requestMapping);

        // New methods for conversion
        Set<Method> newEndpoints = new HashSet<>();
        // New rest calls for conversion
        Set<MethodCall> newRestCalls = new HashSet<>();

        String preURL = "/";

        for(AnnotationExpr annotation : classAnnotations) {
            if(annotation.getNameAsString().equals("RepositoryRestResource")) {
                if(annotation instanceof SingleMemberAnnotationExpr) {
                    preURL += annotation.asSingleMemberAnnotationExpr().getMemberValue().toString();
                    break;
                } else {
                    for(Node node : annotation.getChildNodes()) {
                        System.out.println("t");
                    }
                }
            }
        }

        // For each method that is detected as an endpoint convert into a Method + RestCall
        for(Method method : methods) {

            String url = "/search";
            boolean restResourceFound = false;
            for(Annotation ae : method.getAnnotations()) {
                if(ae.getName().equals("RestResource")) {
                    url = "";
                    restResourceFound = true;
                    break;
                }
            }


            if(!restResourceFound) {
                url += ("/" + method.getName());
            }

            Endpoint endpoint = new Endpoint(method, preURL + url, HttpMethod.GET, getMicroserviceName(sourceFile));
            newEndpoints.add(endpoint);
        }


        // Build the JClass
        return new JClass(
                sourceFile.getName().replace(".java", ""),
                FileUtils.localPathToGitPath(sourceFile.getPath(), config.getRepoName()),
                packageName,
                ClassRole.FEIGN_CLIENT,
                newEndpoints,
                parseFields(cu.findAll(FieldDeclaration.class)),
                parseAnnotations(classAnnotations),
                newRestCalls,
                cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getImplementedTypes().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet()));
    }
}
