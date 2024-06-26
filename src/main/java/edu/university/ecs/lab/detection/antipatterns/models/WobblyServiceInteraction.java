package edu.university.ecs.lab.detection.antipatterns.models;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.Data;

/**
 * Represents a wobbly service interaction.
 */
@Data
public class WobblyServiceInteraction extends AntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Wobbly Service Interaction";

    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "Unpredictable behavior or instability caused by inconsistent communication patterns or unreliable interactions between microservices.";

    /**
     * List of wobbly service interactions in the format: microserviceName.className.methodName
     */
    private List<String> wobblyServiceInteractions;

    /**
     * Constructs a WobblyServiceInteraction object initialized with the given list of interactions.
     *
     * @param wobblyServiceInteractions the list of wobbly service interactions
     */
    public WobblyServiceInteraction(List<String> wobblyServiceInteractions) {
        this.wobblyServiceInteractions = wobblyServiceInteractions;
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
        
        jsonObject.add("Wobbly Service Interactions Found", gson.toJsonTree(wobblyServiceInteractions).getAsJsonArray());
        return jsonObject;
    }
}
