package edu.university.ecs.lab.common.models.ir;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Represents a method declaration in Java.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Method extends Node {
    // Protection Not Yet Implemented
    // protected String protection;

    /**
     * Set of fields representing parameters
     */
    protected Set<Parameter> parameters;

    /**
     * Java return type of the method
     */
    protected String returnType;

    /**
     * The microservice id that this method belongs to
     */
    protected String microserviceName;

    /**
     * Method definition level annotations
     */
    protected Set<Annotation> annotations;

    /**
     * The class id that this method belongs to
     */
    protected String className;

    public Method(String name, String packageAndClassName, Set<Parameter> parameters, String typeAsString, Set<Annotation> annotations, String microserviceName,
                  String className) {
        this.name = name;
        this.packageAndClassName = packageAndClassName;
        this.parameters = parameters;
        this.returnType = typeAsString;
        this.annotations = annotations;
        this.microserviceName = microserviceName;
        this.className = className;
    }

    public Method(MethodDeclaration methodDeclaration) {
        this.name = methodDeclaration.getNameAsString();
        this.packageAndClassName = methodDeclaration.getClass().getPackageName() + "." + methodDeclaration.getClass().getName();
        this.parameters = parseParameters(methodDeclaration.getParameters());
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("packageAndClassName", getPackageAndClassName());
        jsonObject.add("annotations", JsonSerializable.toJsonArray(getAnnotations()));
        jsonObject.add("parameters", JsonSerializable.toJsonArray(getParameters()));
        jsonObject.addProperty("returnType", getReturnType());
        jsonObject.addProperty("microserviceName", microserviceName);
        jsonObject.addProperty("className", className);

        return jsonObject;
    }

    private Set<Parameter> parseParameters(NodeList<com.github.javaparser.ast.body.Parameter> parameters) {
        HashSet<Parameter> parameterSet = new HashSet<>();

        for(com.github.javaparser.ast.body.Parameter parameter : parameters) {
            parameterSet.add(new Parameter(parameter, getPackageAndClassName()));
        }


        return parameterSet;
    }

}
