package edu.university.ecs.lab.delta.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.*;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.*;

/**
 * Represents a system change in the system. It holds all changes made to the system in the form of
 * deltas. Maps are of the form {@literal <localPath
 * (./clonePath/repoName/service/path/to/file.java), Delta>}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemChange implements JsonSerializable {
  /** The microservice id of the changed class */
  private String microserviceName;

  /** The commit id that the delta was generated from */
  private String oldCommit;

  /** The commit id that the delta was generated from */
  private String newCommit;

  /** List of changed controllers */
  private final List<Delta> controllers = new ArrayList<>();

  /** List of changed services */
  private final List<Delta> services = new ArrayList<>();

  /** List of changed repositories */
  private final List<Delta> repositories = new ArrayList<>();



  /**
   * Creates a delta from the given class and entry and adds it to the appropriate map. If the class
   * path already exists in the given map, then the entry is replaced with the new entry. The delta
   * created is returned, null if the change was skipped due to unknown class type.
   *
   * @param jClass class extracted from the CHANGED file
   * @param entry diff entry from git
   * @param localPath path to the class file as ./clonePath/repoName/service/path/to/file.java
   * @return the delta created
   */
  public void addDelta(JClass jClass, DiffEntry entry, String localPath) {
    // Switch through each class role and mark the change
    Delta newDelta = new Delta(localPath, ChangeType.fromDiffEntry(entry), jClass);
    switch (Objects.requireNonNull(jClass).getClassRole()) {
      case CONTROLLER:
        controllers.add(newDelta);
        break;
      case SERVICE:
        services.add(newDelta);
        break;
      case REPOSITORY:
        repositories.add(newDelta);
        break;
      default:
        System.out.println("Skipping change: " + entry.getChangeType() + localPath);
        break;
    }
  }


  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("type", "system");
    jsonObject.addProperty("name", this.getClass().getSimpleName());
    jsonObject.add("controllers", JsonSerializable.toJsonArray(controllers));
    jsonObject.add("services", JsonSerializable.toJsonArray(services));
    jsonObject.add("repositories", JsonSerializable.toJsonArray(repositories));
    jsonObject.addProperty("oldCommit", oldCommit);
    jsonObject.addProperty("newCommit", newCommit);
    jsonObject.addProperty("microserviceName", microserviceName);

    return null;
  }
}
