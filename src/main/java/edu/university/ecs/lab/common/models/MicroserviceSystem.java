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
@EqualsAndHashCode
public class MicroserviceSystem implements JsonSerializable {
  public static final String INITIAL_VERSION = "1.0";

  /** The name of the system */
  private String systemName;

  /** The commit ID of the system */
  private String commitID;

  /** List of microservices in the system */
  private Set<Microservice> microservices;

  /** List of present classes who have no microservice */
  private Set<JClass> orphans;

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
    jsonObject.addProperty("version", commitID);
    jsonObject.add("microservices", JsonSerializable.toJsonArray(microservices));
    jsonObject.add("orphans", JsonSerializable.toJsonArray(orphans));

    return jsonObject;
  }

  /** Increment the version of the system by +0.0.1 */
  @Deprecated
  public void incrementVersion() {
    // split version by '.'
    String[] parts = commitID.split("\\.");

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

    commitID = newVersion.toString();
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
    orphans.addAll(microservice.getEntities());
  }

  /**
   * Given a new or modified microservice, we must adopt awaiting
   * orphans based on their file paths containing the pom.xml's
   * parsed microservice name (indicating they are in the same
   *
   * @param microservice the microservice adopting orphans
   */
  public void adopt(Microservice microservice) {
    Set<JClass> updatedOrphans = new HashSet<>(getOrphans());

    for (JClass jClass : getOrphans()) {
      // If the microservice is in the same folder as the path to the microservice
      if(jClass.getClassPath().contains(microservice.getPath())) {
        microservice.addJClass(jClass);
        updatedOrphans.remove(jClass);
      }

    }

    setOrphans(updatedOrphans);

  }


  public JClass getClassByPath(String path) {
    List<JClass> allClasses = new ArrayList<>();
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getControllers().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getRepositories().stream()).collect(Collectors.toUnmodifiableList()));
    allClasses.addAll(getMicroservices().stream().flatMap(microservice -> microservice.getEntities().stream()).collect(Collectors.toUnmodifiableList()));

    return allClasses.stream().filter(jClass -> jClass.getClassPath().equals(path)).findFirst().orElse(null);
  }


}
