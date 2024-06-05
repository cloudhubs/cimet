package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

import java.util.List;

/** Represents a method declaration in Java. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Method implements JsonSerializable {
  /** Name of the method */
  private String methodName;

  // Protection Not Yet Implemented
  // protected String protection;

  /** List of parameters in the method as a string like: [String userId, String money] */
  private List<Field> parameters;

  /** Java return type of the method */
  private String returnType;

  /** Method definition level annotations * */
  private List<Annotation> annotations;

  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("methodName", methodName);
    jsonObject.add("parameters", JsonSerializable.toJsonArray(parameters));
    jsonObject.addProperty("returnType", returnType);
    jsonObject.add("annotations", JsonSerializable.toJsonArray(annotations));

    return jsonObject;
  }

}
