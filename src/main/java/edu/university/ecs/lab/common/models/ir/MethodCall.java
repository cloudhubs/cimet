package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
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
    protected String objectName;

    /**
     * Name of object this method call is from (Maybe a static class instance, just whatever is before
     * the ".")
     */
    protected String objectType;

    /**
     * Name of method that contains this call
     */
    protected String calledFrom;

    /**
     * Contents within the method call (params) but as a raw string
     */
    protected String parameterContents;

    /**
     * The name of the microservice this MethodCall is called from
     */
    protected String microserviceName;

    /**
     * The class id that this MethodCall is called from
     */
    protected String className;

    public MethodCall(String name, String packageName,String objectType, String objectName, String calledFrom, String parameterContents, String microserviceName,
                      String className) {
        this.name = name;
        this.packageAndClassName = packageName;
        this.objectName = objectName;
        this.objectType = objectType;
        this.calledFrom = calledFrom;
        this.parameterContents = parameterContents;
        this.microserviceName = microserviceName;
        this.className = className;
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
        jsonObject.addProperty("objectType", getObjectType());
        jsonObject.addProperty("parameterContents", getParameterContents());
        jsonObject.addProperty("microserviceName", microserviceName);
        jsonObject.addProperty("className", className);

        return jsonObject;
    }

    public static boolean matchMethod(MethodCall methodCall, Method method) {
        return methodCall.microserviceName.equals(method.microserviceName) && methodCall.objectType.equals(method.className)
                && methodCall.name.equals(method.name);

    }
}
