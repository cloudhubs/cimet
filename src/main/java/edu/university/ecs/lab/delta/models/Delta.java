package edu.university.ecs.lab.delta.models;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Delta implements JsonSerializable {
  /**
   * Relative path to the changed file. This DIFFERS from {@link JClass#getClassPath()} as the
   * jClass path starts at the repoName and this is a working relative path to the file.
   */
  private String localPath;

  /** The type of change that occurred */
  private ChangeType changeType;

  /** The class that was changed, null in the case of ChangeType.DELETE */
  private JClass changedClass;

  private String microserviceName;

  /**
   * Converts the delta object to a JSON object
   *
   * @return the JSON object representation of the delta
   */
  public JsonObject toJsonObject() {
      JsonObject jsonObject = new JsonObject();

      jsonObject.addProperty("changeType", changeType.name());
      jsonObject.addProperty("localPath", localPath);
      jsonObject.addProperty("microserviceName", microserviceName);
      jsonObject.add("changes", changedClass.toJsonObject());

    return jsonObject;
  }

  private Delta() {}


}
