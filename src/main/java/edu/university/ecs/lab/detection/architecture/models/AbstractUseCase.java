package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.delta.models.Delta;

/**
 * Use case class template for all architectural use cases.
 * Children should not host public constructors and all logic
 * should be held in UseCaseInterface methods.
 */
public abstract class AbstractUseCase implements JsonSerializable {

    /**
     * Get the name of the Architectural Use Case
     *
     * @return string name of the Use Case
     */
    public abstract String getName();

    /**
     * Get the description of the Architectural Use Case
     *
     * @return string description of the Use Case
     */
    public abstract String getDescription();


    public abstract JsonObject getMetaData();

    /**
     * Get the weight of the Use Case
     *
     * @return double weight of the Use Case
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
