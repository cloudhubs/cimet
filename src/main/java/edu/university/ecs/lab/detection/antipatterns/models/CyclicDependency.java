package edu.university.ecs.lab.detection.antipatterns.models;


import lombok.Data;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Represents a list of one cycle of Cyclic Dependency Anti-pattern detected
 */
@Data
public class CyclicDependency extends AntiPattern{
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Cyclic Dependency";

    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "When microservices depend on each other in a circular manner, leading to potential deadlock or difficulty in scaling and maintaining the system.";
    
    /**
     * List of one cycle detected
     */
    private List<String> cycle;

    /**
     * Constructs a CyclicDependency object initialized with the given cycle.
     *
     * @param cycle the list of nodes representing the cycle
     */
    public CyclicDependency(List<String> cycle) {
        this.cycle = cycle;
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

        Gson gson = new Gson();

        jsonObject.add(NAME, gson.toJsonTree(cycle).getAsJsonArray());

        return jsonObject;
    }
}