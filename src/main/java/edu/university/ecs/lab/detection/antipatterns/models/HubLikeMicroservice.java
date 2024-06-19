package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a collection of microservices identified as hub-like.
 */
@Data
public class HubLikeMicroservice extends AntiPattern {
    private static final String NAME = "Hub-Like Microservice";
    
    private static final String DSECRIPTION = "A centralized microservice that becomes a bottleneck due to handling too many responsibilities or being a single point of failure.";

    /**
     * Set of microservices identified as hub-like.
     */
    private Set<String> hublikeMicroservices = new HashSet<>();

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
        if (this.hublikeMicroservices.isEmpty()){
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

        jsonObject.add(NAME, gson.toJsonTree(hublikeMicroservices).getAsJsonArray());

        return jsonObject;
    }
}
