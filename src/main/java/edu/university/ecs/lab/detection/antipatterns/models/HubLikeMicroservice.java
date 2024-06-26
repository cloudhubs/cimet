package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.Set;

/**
 * Represents a collection of microservices identified as hub-like.
 */
@Data
public class HubLikeMicroservice extends AntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Hub-Like Microservice";
    
    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "A centralized microservice that becomes a bottleneck due to handling too many responsibilities or being a single point of failure.";

    /**
     * Set of microservices identified as hub-like.
     */
    private Set<String> hublikeMicroservices;

    /**
     * Constructor to initialize with a set of hub-like microservices.
     *
     * @param hublikeMicroservices set of microservices identified as hub-like
     */
    public HubLikeMicroservice(Set<String> hublikeMicroservices) {
        this.hublikeMicroservices = hublikeMicroservices;
    }

    /**
     * Checks if the list of nodes considered hub-like is empty.
     *
     * @return true if the cycle list is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.hublikeMicroservices.isEmpty();
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

        jsonObject.add("Hub-like Microservices Found", gson.toJsonTree(hublikeMicroservices).getAsJsonArray());

        return jsonObject;
    }
}
