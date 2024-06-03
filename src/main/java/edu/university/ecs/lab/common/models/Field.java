package edu.university.ecs.lab.common.models;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.gson.annotations.SerializedName;
import lombok.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/** Represents a field attribute in a Java class or in our case a JClass. */
@Data
@AllArgsConstructor
public class Field implements JsonSerializable {
  /** Java class type of the class variable e.g. String */
  @SerializedName("variableType")
  private String fieldType;

  /** Name of the class variable e.g. username */
  @SerializedName("variableName")
  private String fieldName;

  /**
   * Constructor for object representing a class variable.
   *
   * @param variable the variable declarator representing this field
   */
  public Field(VariableDeclarator variable) {
    setFieldName(variable.getNameAsString());
    setFieldType(variable.getTypeAsString());
  }

  @Override
  public JsonObject toJsonObject() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    builder.add("variableType", getFieldType());
    builder.add("variableName", getFieldName());
    return builder.build();
  }
}
