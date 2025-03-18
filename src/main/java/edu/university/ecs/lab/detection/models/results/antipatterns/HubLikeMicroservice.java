package edu.university.ecs.lab.detection.models.results.antipatterns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents a collection of microservices identified as hub-like.
 */

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("HubLikeMicroservice")
public class HubLikeMicroservice extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Hub-Like Microservice";
    
    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "A centralized microservice that becomes a bottleneck due to handling too many responsibilities or being a single point of failure.";

    /**
     * Set of microservices identified as hub-like.
     */
    private List<String> hublikeMicroservices;

    /**
     * Constructor to initialize with a set of hub-like microservices.
     *
     * @param hublikeMicroservices set of microservices identified as hub-like
     */
    public HubLikeMicroservice(List<String> hublikeMicroservices) {
        this.hublikeMicroservices = hublikeMicroservices;
    }

    /**
     * Checks if the list of nodes considered hub-like is empty.
     *
     * @return true if the cycle list is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.hublikeMicroservices.isEmpty();
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
        return JsonNodeFactory.instance.objectNode().set("Hub-like Microservices Found", objectMapper.valueToTree(hublikeMicroservices));
    }

    public int numHubLike(){
        return hublikeMicroservices.size();
    }
}
