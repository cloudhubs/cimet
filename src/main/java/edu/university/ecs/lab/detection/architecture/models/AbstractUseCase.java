package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import edu.university.ecs.lab.detection.architecture.models.interfaces.UseCaseInterface;

/**
 * Use case class template for all architectural use cases.
 * Children should not host public constructors and all logic
 * should be held in UseCaseInterface methods.
 */
public abstract class AbstractUseCase implements UseCaseInterface {
    protected SystemChange microserviceSystemOld;
    protected MicroserviceSystem microserviceSystemNew;

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

    /**
     * Get the scope of the Architectural Use Case
     *
     * @return scope of the Use Case
     */
    public abstract Scope getScope();

    public abstract JsonObject getMetaData();

    /**
     * Get the weight of the Use Case
     *
     * @return double weight of the Use Case
     */
    public abstract double getWeight();
}
