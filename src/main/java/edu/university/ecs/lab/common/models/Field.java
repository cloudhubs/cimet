package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

/** Represents a field attribute in a Java class or in our case a JClass. */
@Data
@AllArgsConstructor
public class Field implements JsonSerializable {
  /** Java class type of the class variable e.g. String */
  private String fieldType;

  /** Name of the class variable e.g. username */
  private String fieldName;

  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("fieldType", getFieldType());
    jsonObject.addProperty("fieldName", getFieldName());

    return jsonObject;
  }
}
