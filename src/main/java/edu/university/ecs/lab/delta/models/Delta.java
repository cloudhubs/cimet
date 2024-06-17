package edu.university.ecs.lab.delta.models;

import com.google.gson.JsonObject;
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

    private String oldPath;

    private String newPath;

    /**
     * The type of change that occurred
     */
    private ChangeType changeType;

    /** The type of file that was changed */
//  private FileType fileType;

    /**
     * The class that was changed,
     */
    private JClass classChange;

    /**
     * Converts the delta object to a JSON object
     *
     * @return the JSON object representation of the delta
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("changeType", changeType.name());
        jsonObject.addProperty("oldPath", oldPath);
        jsonObject.addProperty("newPath", newPath);
        jsonObject.add("classChange", classChange.toJsonObject());


        return jsonObject;
    }

}
