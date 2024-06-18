package edu.university.ecs.lab.common.models.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Interface for classes that can be serialized to JSON object
 */
public interface JsonSerializable {
    /**
     * This method is a generalizable definition for converting an object of
     * any type to a JsonObject
     *
     * @return a JsonObject representing this
     */
    JsonObject toJsonObject();

    /**
     * This method is a generalizable implementation for converting an iterable of
     * objects that extends this class using {@link this#toJsonObject()} to a JsonArray
     *
     * @param list
     * @return
     */
    static JsonArray toJsonArray(Iterable<? extends JsonSerializable> list) {
        JsonArray jsonArray = new JsonArray();
        for (JsonSerializable item : list) {
            jsonArray.add(item.toJsonObject());
        }
        return jsonArray;
    }

}
