package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a service chain, which is a sequence of services in a network graph.
 */
@Data
public class ServiceChain {
    /**
     * List of services in the chain.
     */
    private List<String> chain = new ArrayList<>();

    /**
     * Constructs a ServiceChain object initialized with the given sequence of services.
     *
     * @param sequence the list of services representing the chain
     */
    public ServiceChain(List<String> sequence) {
        this.chain = sequence;
    }
}