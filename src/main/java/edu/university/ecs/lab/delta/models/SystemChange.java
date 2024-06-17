package edu.university.ecs.lab.delta.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a system change in the system. It holds all changes made to the system in the form of
 * deltas. Maps are of the form {@literal <localPath
 * (./clonePath/repoName/service/path/to/file.java), Delta>}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemChange implements JsonSerializable {

    /**
     * The commit id that the delta was generated from
     */
    private String oldCommit;

    /**
     * The commit id that the delta was generated from
     */
    private String newCommit;

    /**
     * List of changed controllers
     */
    private List<Delta> changes = new ArrayList<>();


    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("changes", JsonSerializable.toJsonArray(changes));
        jsonObject.addProperty("oldCommit", oldCommit);
        jsonObject.addProperty("newCommit", newCommit);

        return null;
    }
}
