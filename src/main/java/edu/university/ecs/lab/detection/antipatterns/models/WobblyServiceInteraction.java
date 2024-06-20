package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.JsonObject;

import lombok.Data;

/**
 * Represents a wobbly service interaction, characterized by a microservice name,
 * class name, and method name.
 */
@Data
public class WobblyServiceInteraction extends AntiPattern{
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Wobbly Service Interaction";

    /**
     * Anti-pattern descsription
     */
    private static final String DESCRIPTION = "Unpredictable behavior or instability caused by inconsistent communication patterns or unreliable interactions between microservices.";
    
    /**
     * The name of the microservice involved in the wobbly service interaction.
     */
    private String microserviceName;
    /**
     * The name of the class where the wobbly service interaction occurs.
     */
    private String className;
    /**
     * The name of the method where the wobbly service interaction occurs.
     */
    private String methodName;

    /**
     * Constructs a WobblyServiceInteraction object initialized with the given parameteres.
     *
     * @param microserviceName  the name of the Microservice
     * @param className         the name of the class where the wobbly service interaction occured
     * @param methodName        the name of the method where the wobbly service interaction occured
     */
    public WobblyServiceInteraction(String microserviceName, String className, String methodName){
        this.microserviceName = microserviceName;
        this.className = className;
        this.methodName = methodName;
    }

    /**
     * Returns a string representation of the wobbly service interaction in the format:
     * microserviceName.className.methodName
     *
     * @return string representation of the wobbly service interaction
     */
    @Override
    public String toString() {
        return microserviceName + "." + className + "." + methodName;
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected JsonObject getMetaData() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Microservice Name", microserviceName);
        jsonObject.addProperty("Class Name", className);
        jsonObject.addProperty("Method Name", methodName);

        return jsonObject;
    }
}
