package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class represents any file in a project's directory
 */
@Data
@EqualsAndHashCode
public abstract class ProjectFile implements JsonSerializable {
    protected String name;
    protected String path;
    protected FileType fileType;


    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("path", path);
        jsonObject.addProperty("fileType", fileType.name());
        return jsonObject;
    }
}
