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

  /** The commit id that the delta was generated from */
  private String oldCommit;

  /** The commit id that the delta was generated from */
  private String newCommit;

  /** List of changed controllers */
  private final List<Delta> changes = new ArrayList<>();



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
  public void addDelta(JClass jClass, DiffEntry entry, String localPath, String microserviceName) {
    // Switch through each class role and mark the change
    Delta newDelta = new Delta(localPath, ChangeType.fromDiffEntry(entry), jClass, microserviceName);
    changes.add(newDelta);
  }


  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("type", "system");
    jsonObject.addProperty("name", this.getClass().getSimpleName());
    jsonObject.add("changes", JsonSerializable.toJsonArray(changes));
    jsonObject.addProperty("oldCommit", oldCommit);
    jsonObject.addProperty("newCommit", newCommit);

    return null;
  }
}
