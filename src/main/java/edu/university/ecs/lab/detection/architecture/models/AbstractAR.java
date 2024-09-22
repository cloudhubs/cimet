package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.serialization.JsonSerializable;

/**
 * Architectural Rule class template for all architectural rules.
 */
public abstract class AbstractAR implements JsonSerializable {

    /**
     * Get the name of the Architectural Rule
     *
     * @return string name of the Architectural Rule
     */
    public abstract String getName();

    /**
     * Get the description of the Architectural Rule
     *
     * @return string description of the Architectural Rule
     */
    public abstract String getDescription();

    /**
     * Get the meta data of the Architectural Rule
     * 
     * @return JSON object meta data of the Architectural Rule
     */
    public abstract JsonObject getMetaData();

    /**
     * Get the weight of the Architectural Rule
     *
     * @return double weight of the Architectural Rule
     */
    public abstract double getWeight();

    /**
     * Get the old commitID
     *
     * @return string old commitID
     */
    public abstract String getOldCommitID();

    /**
     * Get the new commitID
     *
     * @return string new commitID
     */
    public abstract String getNewCommitID();

    /**
     * Get the new commitID
     *
     * @return string new commitID
     */
    public abstract String getType();

    /**
     * Create JSON object with Architectural Rule name, type, old commit ID, new commit ID, and meta data
     * 
     * @return JSON object with Architectural Rule details
     */
    @Override
    public final JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("type", getType());
        jsonObject.addProperty("oldCommitID", getOldCommitID());
        jsonObject.addProperty("newCommitID", getNewCommitID());
        jsonObject.add("metadata", getMetaData());

        return jsonObject;
    }

}
