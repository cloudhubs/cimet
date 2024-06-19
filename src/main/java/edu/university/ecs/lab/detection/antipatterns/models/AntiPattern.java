package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;


/**
 * Abstract implementation of an Antipattern should be the parent
 * of all system Antipatterns
 */
public abstract class AntiPattern implements JsonSerializable {
    protected abstract String getName();
    protected abstract String getDescription();
    protected abstract JsonObject getMetaData();

    @Override
    public final JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("description", getDescription());
        jsonObject.add("metaData", getMetaData());

        return jsonObject;
    }
}
