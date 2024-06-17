package edu.university.ecs.lab.metrics.models.metrics;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

@Data
public class GreedyMicroservice implements JsonSerializable {
    private Set<String> greedyMicroservices = new HashSet<>();

    public GreedyMicroservice(Set<String>greedyMicroservices){
        this.greedyMicroservices = greedyMicroservices;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        Gson gson = new Gson();
        String greedy = gson.toJson(greedyMicroservices);

        jsonObject.addProperty("greedy microservices", greedy);

        return jsonObject;
    }
}
