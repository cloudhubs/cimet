package edu.university.ecs.lab.detection.models.results.antipatterns;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;

import java.util.List;

import lombok.EqualsAndHashCode;


/**
 * Represents a list of one cycle of Cyclic Dependency Anti-pattern detected
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CyclicDependency")
public class CyclicDependency extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "Cyclic Dependency";

    /**
     * Anti-pattern description
     */
    private static final String DESCRIPTION = "When microservices depend on each other in a circular manner, leading to potential deadlock or difficulty in scaling and maintaining the system.";
    
    /**
     * List of one cycle detected
     */
    private List<List<String>> cycles;

    /**
     * Constructs a CyclicDependency object initialized with the given cycle.
     *
     * @param cycles the list of nodes representing the cycle
     */
    public CyclicDependency(List<List<String>> cycles) {
        this.cycles = cycles;
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
        return JsonNodeFactory.instance.objectNode().set("Cyclic Dependencies Found", objectMapper.valueToTree(cycles));
    }

    public int numCyclicDep(){
        return cycles.size();
    }
}