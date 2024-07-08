package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.Data;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Represents a cluster of wrongly interconnected services (Wrong Cuts) detected in a microservice network graph.
 */
@Data
public class WrongCuts extends AntiPattern{
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Wrong Cuts";

    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "Poorly defined boundaries or segmentation of microservices that lead to inefficiencies, increased coupling, or difficulty in scaling and maintaining the system.";
    
    /**
     * Set of service names forming a cluster of wrongly interconnected services.
     */
    private List<String> wrongCuts;

    /**
     * Constructs a WrongCuts object initialized with the provided set of wrongly interconnected service names.
     *
     * @param wrongCuts Set of service names forming a cluster of wrongly interconnected services.
     */
    public WrongCuts(List<String> wrongCuts) {
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

        jsonObject.add("Wrong Cuts Found", gson.toJsonTree(wrongCuts).getAsJsonArray());

        return jsonObject;
    }

    public int numWrongCuts(){
        return wrongCuts.size();
    }
}
