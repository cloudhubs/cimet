package edu.university.ecs.lab.detection.models.results.antipatterns;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the "No Health Check" anti-pattern
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("NoHealthcheck")
public class NoHealthcheck extends AbstractAntiPattern {
    /**
     * Anti-pattern name
     */
    private static final String NAME = "No Health Check";
    
    /**
     * Anti-pattern descsription
     */
    private static final String DESCRIPTION = "The lack of mechanisms for monitoring the health and availability of microservices, which can result in undetected failures and decreased system reliability.";

    /**
     * Flag indicating whether the anti-pattern is present
     */
    private Map<String, Boolean> noHealthcheck;

    /**
     * Constructs a NoHealthcheck object with the specified flag indicating the presence of the anti-pattern.
     *
     * @param noHealthcheck boolean flag indicating whether the "No Health Check" anti-pattern is present
     */
    public NoHealthcheck(Map<String, Boolean> noHealthcheck){
        this.noHealthcheck = noHealthcheck;
    }

    /**
     * Retrieves the flag indicating the presence of the "No Health Check" anti-pattern.
     *
     * @return boolean flag indicating whether the "No Health Check" anti-pattern is present
     */
    @JsonIgnore
    public Map<String, Boolean> getnoHealthcheck(){
        return this.noHealthcheck;
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
        return JsonNodeFactory.instance.objectNode().set("Microservices and Healthchecks Found", objectMapper.valueToTree(noHealthcheck));
    }

    public int numNoHealthChecks(){
        int count = 0;
        for (Map.Entry<String, Boolean> entry : noHealthcheck.entrySet()){
            if (entry.getValue() == false){
                count++;
            }
        }
        return count;
    }
}
