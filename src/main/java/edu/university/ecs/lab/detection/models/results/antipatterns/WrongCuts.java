package edu.university.ecs.lab.detection.models.results.antipatterns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;

import java.util.List;

import lombok.EqualsAndHashCode;

/**
 * Represents a cluster of wrongly interconnected services (Wrong Cuts) detected in a microservice network graph.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("WrongCuts")
public class WrongCuts extends AbstractAntiPattern {
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
        return JsonNodeFactory.instance.objectNode().set("Wrong Cuts Found", objectMapper.valueToTree(wrongCuts));
    }

    public int numWrongCuts(){
        return wrongCuts.size();
    }

    public String toString() {
        return getMetaData().toString();
    }
}
