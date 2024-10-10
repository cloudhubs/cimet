package edu.university.ecs.lab.detection.architecture.models;

import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.ir.RestCall;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

/**
 * Architectural Rule 3 Class: Floating call due to invalid call creation
 */
@Data
public class AR3 extends AbstractAR {
    
    /**
     * Architectural Rule 3 details 
     */ 
    protected static final String TYPE = "Architectural Rule 3";
    protected static final String NAME = "Floating call due to invalid call creation";
    protected static final String DESC = "A rest call is added that references a nonexistent endpoint";
    
    
    private String oldCommitID;
    private String newCommitID;
    protected JsonObject metaData;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    /**
     * Scan and compare old microservice system and new microservice system to identify invalid call creation
     * 
     * @param delta change between old commit and new microservice systems
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list of rest calls with no match found in new system
     */
    public List<AR3> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<AR3> archRules = new ArrayList<>();

        if(delta.getClassChange() == null) {
            return new ArrayList<>();
        }
        // If it isn't add or modify, or not a service
        if (delta.getChangeType().equals(ChangeType.DELETE) || !delta.getClassChange().getClassRole().equals(ClassRole.SERVICE)) {
            return archRules;
        }


        // For each restCall if we don't find a match in the new System
        // TODO this technically includes RestCalls that have already been flagged in the past
        for (RestCall restCall : delta.getClassChange().getRestCalls()) {
            if (!findMatch(restCall, newSystem)) {
                AR3 archRule3 = new AR3();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("RestCall", restCall.getID());
                archRule3.setMetaData(jsonObject);
                archRule3.setOldCommitID(oldSystem.getCommitID());
                archRule3.setNewCommitID(newSystem.getCommitID());
                archRules.add(archRule3);
            }
        }

        // Return list of rest calls (added or modified in a service) without a match found in new system
        return archRules;
    }

    /**
     * Find rest call/endpoint matched in the given system
     * 
     * @param restCall call to be matched to an endpoint
     * @param newSystem microservice system to search for match in
     * @return true if an endpoint match is found, false otherwise
     */
    private static boolean findMatch(RestCall restCall, MicroserviceSystem newSystem) {
        for (Microservice microservice : newSystem.getMicroservices()) {
            for (JClass controller : microservice.getControllers()) {
                for (Endpoint endpoint : controller.getEndpoints()) {
                    if (RestCall.matchEndpoint(restCall, endpoint)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Scan and compare old microservice system and new microservice system to identify invalid call creation
     * 
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list of all rest calls without an endpoint match in new system
     */
    public static List<AR3> scan2(MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<AR3> archRules = new ArrayList<>();

        List<RestCall> allRestCalls = newSystem.getMicroservices().stream().flatMap(microservice -> microservice.getRestCalls().stream()).collect(Collectors.toList());


        // For each restCall if we don't find a match in the new System
        // TODO this technically includes RestCalls that have already been flagged in the past
        for (RestCall restCall : allRestCalls) {
            if (!findMatch(restCall, newSystem)) {
                AR3 archRule3 = new AR3();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("RestCall", restCall.toJsonObject());
                archRule3.setMetaData(jsonObject);
                archRule3.setOldCommitID(oldSystem.getCommitID());
                archRule3.setNewCommitID(newSystem.getCommitID());
                archRules.add(archRule3);
            }
        }

        // Return list of all rest calls without an endpoint match in new system
        return archRules;
    }
}

