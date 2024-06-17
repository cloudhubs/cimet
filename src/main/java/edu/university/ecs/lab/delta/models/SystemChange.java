package edu.university.ecs.lab.delta.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the overall change in the IR from oldCommit
 * to newCommit as a list of Deltas see {@link Delta}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemChange implements JsonSerializable {

    /**
     * The old commitID
     */
    private String oldCommit;

    /**
     * The new commitID
     */
    private String newCommit;

    /**
     * List of delta changes
     */
    private List<Delta> changes = new ArrayList<>();


    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("changes", JsonSerializable.toJsonArray(changes));
        jsonObject.addProperty("oldCommit", oldCommit);
        jsonObject.addProperty("newCommit", newCommit);

        return jsonObject;
    }
}
