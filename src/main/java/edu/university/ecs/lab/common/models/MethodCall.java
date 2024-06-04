package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import lombok.*;

/**
 * Represents a method call in Java. Method call looks like: objectName.methodName() inside of
 * calledFrom
 */
@Data
@AllArgsConstructor
public class MethodCall implements JsonSerializable {
  /** Name of the called method */
  private String methodName;

  /**
   * Name of object this method call is from (Maybe a static class instance, just whatever is before
   * the ".")
   */
  private String objectName;

  /** Name of method that contains this call */
  private String calledFrom;

  /**
   * Contents within the method call (params) but as a raw string
   */
  private String parameterContents;

  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("methodName", getMethodName());
    jsonObject.addProperty("objectName", getObjectName());
    jsonObject.addProperty("calledFrom", getCalledFrom());
    jsonObject.addProperty("parameterContents", getParameterContents());

    return jsonObject;
  }
}
