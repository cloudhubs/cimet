package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

import static edu.university.ecs.lab.common.utils.ObjectToJsonUtils.listToJsonArray;

/** Represents a method declaration in Java. */
@Data
@AllArgsConstructor
public class Method implements JsonSerializable {
  /** Name of the method */
  protected String methodName;

  // Protection Not Yet Implemented
  // protected String protection;

  /** List of parameters in the method as a string like: [String userId, String money] */
  @SerializedName("parameter")
  protected List<Field> parameters;

  /** Java return type of the method */
  protected String returnType;

  /** Method definition level annotations * */
  protected List<Annotation> annotations;

  @Override
  public JsonObject toJsonObject() {
    return createBuilder().build();
  }

  protected JsonObjectBuilder createBuilder() {
    JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

    methodObjectBuilder.add("methodName", methodName);
    methodObjectBuilder.add("parameter", parameters.toString());
    methodObjectBuilder.add("returnType", returnType);
    methodObjectBuilder.add("annotations", listToJsonArray(annotations));

    return methodObjectBuilder;
  }
}
