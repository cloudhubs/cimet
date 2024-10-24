package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.Data;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Represents a service chain, which is a sequence of services in a network graph.
 */
@Data
public class ServiceChain extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Service Chain";


    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "A series of microservices linked in a sequence where each service depends on the output of the previous one, potentially introducing latency and complexity.";
    
    /**
     * List of services in the chain.
     */
    private List<List<String>> chain;

    /**
     * Constructs a ServiceChain object initialized with the given sequence of services.
     *
     * @param sequence the list of services representing the chain
     */
    public ServiceChain(List<List<String>> sequence) {
        this.chain = sequence;
    }

    /**
     * Checks if the list of nodes considered greedy is empty.
     *
     * @return true if the list of nodes is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.chain.isEmpty();
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

        jsonObject.add("Service Chains Found", gson.toJsonTree(chain).getAsJsonArray());

        return jsonObject;
    }

    public int numServiceChains(){
        return chain.size();
    }
}