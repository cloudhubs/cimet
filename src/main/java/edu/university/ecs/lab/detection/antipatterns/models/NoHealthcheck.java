package edu.university.ecs.lab.detection.antipatterns.models;

import java.util.Map;

import com.google.gson.Gson;
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
    public Map<String, Boolean> getnoHealthcheck(){
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
        Gson gson = new Gson();

        jsonObject.add("Microservices and Healthchecks Found", gson.toJsonTree(noHealthcheck).getAsJsonObject());

        return jsonObject;
    }
}
