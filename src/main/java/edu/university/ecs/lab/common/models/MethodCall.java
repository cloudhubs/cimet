package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a method call in Java.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class MethodCall extends Node {

    /**
     * Name of object this method call is from (Maybe a static class instance, just whatever is before
     * the ".")
     */
    private String objectName;

    /**
     * Name of method that contains this call
     */
    private String calledFrom;

    /**
     * Contents within the method call (params) but as a raw string
     */
    private String parameterContents;

    public MethodCall(String name, String packageName, String objectName, String calledFrom, String parameterContents) {
        this.name = name;
        this.packageAndClassName = packageName;
        this.objectName = objectName;
        this.calledFrom = calledFrom;
        this.parameterContents = parameterContents;
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("packageAndClassName", getPackageAndClassName());
        jsonObject.addProperty("objectName", getObjectName());
        jsonObject.addProperty("calledFrom", getCalledFrom());
        jsonObject.addProperty("parameterContents", getParameterContents());

        return jsonObject;
    }
}
