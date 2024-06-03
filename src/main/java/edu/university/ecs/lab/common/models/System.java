package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static edu.university.ecs.lab.common.utils.ObjectToJsonUtils.listToJsonArray;

/** Represents the intermediate structure of a microservice system. */
@Data
@AllArgsConstructor
public class System implements JsonSerializable {
  /** The name of the system */
  private String systemName;

  /** The version of the system like v0.0.1 */
  private String version;

  /** List of microservices in the system */
  @SerializedName("microservices")
  private List<Microservice> msList;

  /**
   * Get a map of microservices in the system as {@literal <msId (service name), Microservice>}
   *
   * @return the map of microservices
   */
  public Map<String, Microservice> getServiceMap() {
    Map<String, Microservice> msMap = new LinkedHashMap<>();

    for (Microservice ms : msList) {
      msMap.put(ms.getId(), ms);
    }

    return msMap;
  }

  /**
   * Construct a JSON object representing the given ms system name, version, and microservice data
   * map.
   *
   * @return the constructed JSON object
   */
  @Override
  public JsonObject toJsonObject() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    builder.add("systemName", systemName);
    builder.add("version", version);
    builder.add("microservices", listToJsonArray(msList));

    return builder.build();
  }

  /** Increment the version of the system by +0.0.1 */
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
}
