package edu.university.ecs.lab.common.models.ir;

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
    protected Set<Field> parameters;

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
    protected List<Annotation> annotations;

    public Method(String name, String packageAndClassName, Set<Field> parameters, String typeAsString, List<Annotation> annotations, String microserviceName) {
        this.name = name;
        this.packageAndClassName = packageAndClassName;
        this.parameters = parameters;
        this.returnType = typeAsString;
        this.annotations = annotations;
        this.microserviceName = microserviceName;
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

        return jsonObject;
    }

}
