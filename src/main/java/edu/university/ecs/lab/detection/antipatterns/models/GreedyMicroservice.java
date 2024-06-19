package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a collection of microservices identified as greedy.
 */
@Data
public class GreedyMicroservice implements JsonSerializable {
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
     * see {@link JsonSerializable#toJsonObject()}
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        Gson gson = new Gson();
        String greedy = gson.toJson(greedyMicroservices);

        jsonObject.addProperty("greedy microservices", greedy);

        return jsonObject;
    }
}
