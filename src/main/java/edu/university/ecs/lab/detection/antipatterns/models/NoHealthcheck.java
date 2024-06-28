package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.JsonObject;

import lombok.Data;

/**
 * Represents the "No Health Check" anti-pattern
 */
@Data
public class NoHealthcheck extends AntiPattern{
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
    private boolean noHealthcheck;

    /**
     * Constructs a NoHealthcheck object with the specified flag indicating the presence of the anti-pattern.
     *
     * @param noHealthcheck boolean flag indicating whether the "No Health Check" anti-pattern is present
     */
    public NoHealthcheck(boolean noHealthcheck){
        this.noHealthcheck = noHealthcheck;
    }

    /**
     * Retrieves the flag indicating the presence of the "No Health Check" anti-pattern.
     *
     * @return boolean flag indicating whether the "No Health Check" anti-pattern is present
     */
    public boolean getnoHealthcheck(){
        return this.noHealthcheck;
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

        jsonObject.addProperty("Healthchecks Found", noHealthcheck);

        return jsonObject;
    }
}
