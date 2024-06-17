package edu.university.ecs.lab.common.models.serialization;

import com.google.gson.*;

import java.util.List;

/** Interface for classes that can be serialized to JSON object */
public interface JsonSerializable {
  JsonObject toJsonObject();

  static JsonArray toJsonArray(Iterable<? extends JsonSerializable> list) {
    JsonArray jsonArray = new JsonArray();
    for (JsonSerializable item : list) {
      jsonArray.add(item.toJsonObject());
    }
    return jsonArray;
  }

}
