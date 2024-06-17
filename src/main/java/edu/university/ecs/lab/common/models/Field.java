package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a field attribute in a Java class or in our case a JClass.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Field implements JsonSerializable {
    /**
     * Java class type of the class variable e.g. String
     */
    private String type;

    /**
     * Name of the class variable
     */
    private String name;

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("type", getType());

        return jsonObject;
    }
}
