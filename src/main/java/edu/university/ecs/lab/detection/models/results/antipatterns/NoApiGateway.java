package edu.university.ecs.lab.detection.models.results.antipatterns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the "No API-Gateway" anti-pattern
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("NoApiGateway")
public class NoApiGateway extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "No API-Gateway";
    
    /**
     * Anti-pattern descsription
     */
    private static final String DESCRIPTION = "The absence of a centralized entry point for managing, routing, and securing API calls, leading to potential inefficiencies and security vulnerabilities.";

    /**
     * Flag indicating whether the anti-pattern is present
     */
    private boolean noApiGateway;

    /**
     * Constructs a NoApiGateway object with the specified flag indicating the presence of the anti-pattern.
     *
     * @param noApiGateway boolean flag indicating whether the "No API-Gateway" anti-pattern is present
     */
    public NoApiGateway(boolean noApiGateway){
        this.noApiGateway = noApiGateway;
    }

    /**
     * Retrieves the flag indicating the presence of the "No API-Gateway" anti-pattern.
     *
     * @return boolean flag indicating whether the "No API-Gateway" anti-pattern is present
     */
    public boolean getnoApiGateway(){
        return this.noApiGateway;
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
        return JsonNodeFactory.instance.objectNode().set("No API-Gateway:", objectMapper.valueToTree(noApiGateway));
    }

    public int getBoolApiGateway(){
        if (noApiGateway){
            return 1;
        }
        else{
            return 0;
        }
    }
    
}
