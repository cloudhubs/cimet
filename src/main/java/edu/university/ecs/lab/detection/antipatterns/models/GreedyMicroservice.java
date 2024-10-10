package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.List;


/**
 * Represents a collection of microservices identified as greedy.
 */
@Data
public class GreedyMicroservice extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Greedy Microservice";

    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "A microservice that overextends its responsibilities, violating the principle of single responsibility and potentially leading to increased complexity, dependencies, and maintenance challenges within the system.";
    /**
     * Set of microservices identified as greedy
     */
    private List<String> greedyMicroservices;

    /**
     * Constructor to initialize with a set of greedy microservices.
     *
     * @param greedyMicroservices set of microservices identified as greedy
     */
    public GreedyMicroservice(List<String> greedyMicroservices) {
        this.greedyMicroservices = greedyMicroservices;
    }

    /**
     * Checks if the list of nodes considered greedy is empty.
     *
     * @return true if the list of nodes is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.greedyMicroservices.isEmpty();
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

        jsonObject.add("Greedy Microservices Found", gson.toJsonTree(greedyMicroservices).getAsJsonArray());

        return jsonObject;
    }

    public int numGreedyMicro(){
        return greedyMicroservices.size();
    }
}
