package edu.university.ecs.lab.detection.models.results.antipatterns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;

import java.util.List;

import lombok.EqualsAndHashCode;

/**
 * Represents a service chain, which is a sequence of services in a network graph.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("ServiceChain")
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
    @JsonIgnore
    protected String getName() {
        return NAME;
    }

    @Override
    @JsonIgnore
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    @JsonIgnore
    protected JsonNode getMetaData() {
        return JsonNodeFactory.instance.objectNode().set("Service Chains Found", objectMapper.valueToTree(chain));
    }

    public int numServiceChains(){
        return chain.size();
    }

    public String toString() {
        return chain.toString();
    }
}