package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Represents a cluster of wrongly interconnected services (Wrong Cuts) detected in a microservice network graph.
 */
@Data
public class WrongCuts extends AntiPattern{
    private static final String NAME = "Wrong Cuts";

    private static final String DESCRIPTION = "Poorly defined boundaries or segmentation of microservices that lead to inefficiencies, increased coupling, or difficulty in scaling and maintaining the system.";
    
    /**
     * Set of service names forming a cluster of wrongly interconnected services.
     */
    private Set<String> wrongCuts = new HashSet<>();

    /**
     * Constructs a WrongCuts object initialized with the provided set of wrongly interconnected service names.
     *
     * @param wrongCuts Set of service names forming a cluster of wrongly interconnected services.
     */
    public WrongCuts(Set<String> wrongCuts) {
        this.wrongCuts = wrongCuts;
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

        jsonObject.add(NAME, gson.toJsonTree(wrongCuts).getAsJsonArray());

        return jsonObject;
    }
}
