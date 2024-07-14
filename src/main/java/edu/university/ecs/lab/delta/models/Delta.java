package edu.university.ecs.lab.delta.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.ProjectFile;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class represents a single Delta change between two commits.
 * In the case of ChangeType.DELETE @see {@link ChangeType} the
 * classChange will respectively be null as the instance of this class
 * is no longer locally present for parsing at the new commit
 */
@Data
@AllArgsConstructor
public class Delta implements JsonSerializable {

    /**
     * The new path to the file changed/added
     * Note: The path may be null in the event of an add
     */
    private String oldPath;

    /**
     * The old path to the file changed/added
     * Note: The path may be null in the event of an delete
     */
    private String newPath;

    /**
     * The type of change that occurred
     */
    private ChangeType changeType;

    /**
     * The changed contents, could be a changed class or
     * a changed configuration file
     */
    private JsonObject data;

    public JClass getClassChange() {
        Gson gson = new Gson();
        if(data.has("jClass")) {
            return gson.fromJson(data.get("jClass"), JClass.class);
        } else {
            return null;
        }
    }

    public ConfigFile getConfigChange() {
        Gson gson = new Gson();
        if(data.has("config")) {
            return gson.fromJson(data.get("config"), ConfigFile.class);
        } else {
            return null;
        }
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("changeType", changeType.name());
        jsonObject.addProperty("oldPath", oldPath);
        jsonObject.addProperty("newPath", newPath);
        jsonObject.add("data", data);

        return jsonObject;
    }

}
