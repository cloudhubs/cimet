package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Represents a method declaration in Java.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Method implements JsonSerializable {
    /**
     * Name of the method
     */
    private String name;

    // Protection Not Yet Implemented
    // protected String protection;

    /**
     * Set of fields representing parameters
     */
    private Set<Field> parameters;

    /**
     * Java return type of the method
     */
    private String returnType;

    /**
     * Method definition level annotations
     */
    private Set<Annotation> annotations;

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", name);
        jsonObject.add("annotations", JsonSerializable.toJsonArray(annotations));
        jsonObject.add("parameters", JsonSerializable.toJsonArray(parameters));
        jsonObject.addProperty("returnType", returnType);

        return jsonObject;
    }

}
