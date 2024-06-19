package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a wobbly service interaction, characterized by a microservice name,
 * class name, and method name.
 */
@Data
@AllArgsConstructor
public class WobblyServiceInteraction {
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
     * Returns a string representation of the wobbly service interaction in the format:
     * microserviceName.className.methodName
     *
     * @return string representation of the wobbly service interaction
     */
    @Override
    public String toString() {
        return microserviceName + "." + className + "." + methodName;
    }
}
