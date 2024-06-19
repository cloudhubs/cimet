package edu.university.ecs.lab.detection.antipatterns.models;

import com.google.gson.JsonObject;

import lombok.Data;

/**
 * Represents the "No API-Gateway" anti-pattern
 */
@Data
public class NoApiGateway extends AntiPattern{
    private static final String NAME = "No API-Gateway";
    
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

        jsonObject.addProperty(NAME, noApiGateway);

        return jsonObject;
    }
    
}
