package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a cluster of wrongly interconnected services (Wrong Cuts) detected in a microservice network graph.
 */
@Data
public class WrongCuts {
    /**
     * Set of service names forming a cluster of wrongly interconnected services.
     */
    private Set<String> wrongCuts = new HashSet<>();

    /**
     * Constructs a WrongCuts object initialized with the provided set of wrongly interconnected service names.
     *
     * @param wrongCuts Set of service names forming a cluster of wrongly interconnected services.
     */
    public WrongCuts(Set<String> wrongCuts) {
        this.wrongCuts = wrongCuts;
    }
}
