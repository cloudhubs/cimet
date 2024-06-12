package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

import java.util.*;
import java.util.regex.Pattern;
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

  /** List of present classes who have no microservice */
  private List<JClass> orphans;

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
    jsonObject.add("orphans", JsonSerializable.toJsonArray(orphans));

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

  /**
   * Returns the microservice that matches the passed name
   *
   * @param name the name to search for
   * @return microservice whose name matches or null if not found
   */
  public Microservice findMicroserviceByName(String name) {
    return getMicroservices().stream().filter(microservice -> microservice.getName().equals(name)).findFirst().orElse(null);
  }


  /**
   * Given an existing microservice, if it must now be orphanized
   * then all JClasses belonging to that service will be added to
   * the system's pool of orphans for later use
   *
   * @param microservice the microservice to orphanize
   */
  public void orphanize(Microservice microservice) {
    orphans.addAll(microservice.getControllers());
    orphans.addAll(microservice.getServices());
    orphans.addAll(microservice.getRepositories());
  }

  /**
   * Given a new or modified microservice, we must adopt awaiting
   * orphans based on their file paths containing the pom.xml's
   * parsed microservice name (indicating they are in the same
   *
   * @param microservice the microservice adopting orphans
   */
  public void adopt(Microservice microservice, String microserviceName) {
    for (JClass jClass : getOrphans()) {
      // Match the pattern "<whatever>\\<microserviceName>\\<className>.java"
      if(Pattern.matches(".*\\\\" + microserviceName + "\\\\.*\\.java", jClass.getClassPath())) {
        microservice.addJClass(jClass);
      }

    }

  }


  public JClass getClassByPath(String path) {
    List<JClass> allClasses = new ArrayList<>();
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getControllers().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getRepositories().stream()).collect(Collectors.toUnmodifiableList()));

    return allClasses.stream().filter(jClass -> jClass.getClassPath().equals(path)).findFirst().orElse(null);
  }
}
