package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/** Represents the intermediate structure of a microservice system. */
@Data
@AllArgsConstructor
public class MicroserviceSystem implements JsonSerializable {
  public static final String INITIAL_VERSION = "1.0";

  /** The name of the system */
  private String systemName;

  /** The version of the system like v0.0.1 */
  private String version;

  /** List of microservices in the system */
  private List<Microservice> microservices;

  /**
   * Construct a JSON object representing the given ms system name, version, and microservice data
   * map.
   *
   * @return the constructed JSON object
   */
  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("systemName", systemName);
    jsonObject.addProperty("version", version);
    jsonObject.add("microservices", JsonSerializable.toJsonArray(microservices));

    return jsonObject;
  }

  /** Increment the version of the system by +0.0.1 */
  @Deprecated
  public void incrementVersion() {
    // split version by '.'
    String[] parts = version.split("\\.");

    // cast version string parts to integer
    int[] versionParts = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      versionParts[i] = Integer.parseInt(parts[i]);
    }

    // increment end digit
    versionParts[versionParts.length - 1]++;

    // end digit > 9? increment middle and reset end digit to 0
    if (versionParts[versionParts.length - 1] == 10) {
      versionParts[versionParts.length - 1] = 0;
      versionParts[versionParts.length - 2]++;

      // middle digit > 9, increment start digit (major version) and reset middle to 0
      if (versionParts[versionParts.length - 2] == 10) {
        versionParts[versionParts.length - 2] = 0;
        versionParts[0]++;
      }
    }

    StringBuilder newVersion = new StringBuilder();
    for (int i = 0; i < versionParts.length; i++) {
      newVersion.append(versionParts[i]);
      if (i < versionParts.length - 1) {
        newVersion.append('.');
      }
    }

    version = newVersion.toString();
  }


  public JClass getClassByPath(String path) {
    List<JClass> allClasses = new ArrayList<>();
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getControllers().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getRepositories().stream()).collect(Collectors.toUnmodifiableList()));

    return allClasses.stream().filter(jClass -> jClass.getClassPath().equals(path)).findFirst().orElse(null);
  }
}
