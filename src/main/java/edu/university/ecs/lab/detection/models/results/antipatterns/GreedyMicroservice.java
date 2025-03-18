package edu.university.ecs.lab.detection.models.results.antipatterns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * Represents a collection of microservices identified as greedy.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("GreedyMicroservice")
public class GreedyMicroservice extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Greedy Microservice";

    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "A microservice that overextends its responsibilities, violating the principle of single responsibility and potentially leading to increased complexity, dependencies, and maintenance challenges within the system.";
    /**
     * Set of microservices identified as greedy
     */
    private List<String> greedyMicroservices;

    /**
     * Constructor to initialize with a set of greedy microservices.
     *
     * @param greedyMicroservices set of microservices identified as greedy
     */
    public GreedyMicroservice(List<String> greedyMicroservices) {
        this.greedyMicroservices = greedyMicroservices;
    }

    /**
     * Checks if the list of nodes considered greedy is empty.
     *
     * @return true if the list of nodes is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.greedyMicroservices.isEmpty();
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
        return JsonNodeFactory.instance.objectNode().set("Greedy Microservices Found", objectMapper.valueToTree(greedyMicroservices));
    }

    public int numGreedyMicro(){
        return greedyMicroservices.size();
    }
}
