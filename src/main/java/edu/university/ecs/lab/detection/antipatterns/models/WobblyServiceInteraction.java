package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a wobbly service interaction.
 */
@Data
@AllArgsConstructor
public class WobblyServiceInteraction {
    private String microserviceName;
    private String className;
    private String methodName;

    @Override
    public String toString() {
        return microserviceName + "." + className + "." + methodName;
    }
}
