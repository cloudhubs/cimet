package edu.university.ecs.lab.common.models.serialization;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.ir.*;

import java.lang.reflect.Type;

public class ProjectFileDeserializer implements JsonDeserializer<ProjectFile>  {

    @Override
    public ProjectFile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("fileType").getAsString();
        switch (type) {
            case "JCLASS":
                return context.deserialize(json, JClass.class);
            case "POM":
                return context.deserialize(json, ConfigFile.class);
            case "CONFIG":
                return context.deserialize(json, ConfigFile.class);
            default:
                throw new JsonParseException("Unsupported type: " + type);
        }
    }


}
