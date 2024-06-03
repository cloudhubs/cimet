package edu.university.ecs.lab.common.models;

import lombok.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/** Represents an annotation in Java */
@Data
@AllArgsConstructor
public class Annotation implements JsonSerializable {
  /** The name of the annotation * */
  protected String annotationName;

  /** The contents of the annotation * */
  protected String contents;

  @Override
  public JsonObject toJsonObject() {
    return createBuilder().build();
  }

  protected JsonObjectBuilder createBuilder() {
    JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

    methodObjectBuilder.add("annotationName", annotationName);
    methodObjectBuilder.add("contents", contents);

    return methodObjectBuilder;
  }
}
