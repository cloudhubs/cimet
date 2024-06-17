package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents an annotation in Java
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Annotation implements JsonSerializable {
    /**
     * The name of the annotation *
     */
    protected String name;

    /**
     * The contents of the annotation *
     */
    protected String contents;

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("contents", getContents());

        return jsonObject;
    }
}
