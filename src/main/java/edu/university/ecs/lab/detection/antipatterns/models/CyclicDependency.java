package edu.university.ecs.lab.detection.antipatterns.models;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of one cycle of Cyclic Dependency Anti-pattern detected
 */
@Data
public class CyclicDependency {
    /**
     * List of one cycle detected
     */
    private List<String> cycle = new ArrayList<>();

    /**
     * Constructs a CyclicDependency object initialized with the given cycle.
     *
     * @param cycle the list of nodes representing the cycle
     */
    public CyclicDependency(List<String> cycle) {
        this.cycle = cycle;
    }
}