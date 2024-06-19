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
public class HubLikeMicroservice implements JsonSerializable {
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
     * see {@link JsonSerializable#toJsonObject()}
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        Gson gson = new Gson();
        String hublike = gson.toJson(hublikeMicroservices);

        jsonObject.addProperty("hub-like microservices", hublike);

        return jsonObject;
    }
}
