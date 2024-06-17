package edu.university.ecs.lab.metrics.models.metrics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class HubLikeMicroservice implements JsonSerializable {
    private Set<String> hublikeMicroservices = new HashSet<>();

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
