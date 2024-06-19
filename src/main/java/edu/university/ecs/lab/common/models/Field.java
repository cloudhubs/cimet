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
@EqualsAndHashCode
public class Field extends Node {
    /**
     * Java class type of the class variable e.g. String
     */
    private String type;

    public Field(String name, String packageAndClassName, String type) {
        this.name = name;
        this.packageAndClassName = packageAndClassName;
        this.type = type;
    }


    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("packageAndClassName", getPackageAndClassName());
        jsonObject.addProperty("type", getType());

        return jsonObject;
    }
}
