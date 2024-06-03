package edu.university.ecs.lab.common.models;

import lombok.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Represents a method call in Java. Method call looks like: objectName.methodName() inside of
 * calledFrom
 */
@Data
@AllArgsConstructor
public class MethodCall implements JsonSerializable {
  /** Name of the called method */
  protected String methodName;

  /**
   * Name of object this method call is from (Maybe a static class instance, just whatever is before
   * the ".")
   */
  protected String objectName;

  /** Name of method that contains this call */
  protected String calledFrom;

  @Override
  public JsonObject toJsonObject() {
    return createBuilder().build();
  }

  protected JsonObjectBuilder createBuilder() {
    JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

    methodObjectBuilder.add("methodName", this.methodName);
    methodObjectBuilder.add("objectName", this.objectName);
    methodObjectBuilder.add("calledFrom", this.calledFrom);

    return methodObjectBuilder;
  }
}
