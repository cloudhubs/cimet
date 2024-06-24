package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;


/**
 * Represents a collection of microservices identified as greedy.
 */
@Data
public class GreedyMicroservice extends AntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Greedy Microservice";

    /**
     * Anti-pattern descsription
     */
    private static final String DSECRIPTION = "A microservice that overextends its responsibilities, violating the principle of single responsibility and potentially leading to increased complexity, dependencies, and maintenance challenges within the system.";
    /**
     * Set of microservices identified as greedy
     */
    private Set<String> greedyMicroservices = new HashSet<>();

    /**
     * Constructor to initialize with a set of greedy microservices.
     *
     * @param greedyMicroservices set of microservices identified as greedy
     */
    public GreedyMicroservice(Set<String> greedyMicroservices) {
        this.greedyMicroservices = greedyMicroservices;
    }

    /**
     * Checks if the list of nodes considered greedy is empty.
     *
     * @return true if the cycle list is empty, false otherwise
     */
    public boolean isEmpty(){
        if (this.greedyMicroservices.isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected String getDescription() {
        return DSECRIPTION;
    }

    @Override
    protected JsonObject getMetaData() {
        JsonObject jsonObject = new JsonObject();

        Gson gson = new Gson();

        jsonObject.add(NAME, gson.toJsonTree(greedyMicroservices).getAsJsonArray());

        return jsonObject;
    }
}
