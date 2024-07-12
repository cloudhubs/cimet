package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

@Data
public class ConfigFile extends ProjectFile implements JsonSerializable {
    JsonObject data;

    public ConfigFile(String path, String name, JsonObject data, FileType type) {
        this.path = path;
        this.name = name;
        this.data = data;
        this.fileType = type;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();
        jsonObject.add("data", data);
        return jsonObject;
    }
}
