package edu.university.ecs.lab.detection.models.results.antipatterns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a wobbly service interaction.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("WobblyServiceInteraction")
public class WobblyServiceInteraction extends AbstractAntiPattern {
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
        return JsonNodeFactory.instance.objectNode().set("Wobbly Service Interactions Found", objectMapper.valueToTree(wobblyServiceInteractions));
    }

    public int numWobbblyService(){
        return wobblyServiceInteractions.size();
    }
}
