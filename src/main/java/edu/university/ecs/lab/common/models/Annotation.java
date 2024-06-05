package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

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
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("annotationName", getAnnotationName());
    jsonObject.addProperty("contents", getContents());

    return jsonObject;
  }
}
