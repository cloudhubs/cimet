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
 *
 */
@Data
public class UseCase3 extends AbstractUseCase {
    protected static final String TYPE = "UseCase3";
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

    public List<UseCase3> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<UseCase3> useCases = new ArrayList<>();

        // If it isn't add or modify, or not a service
        if (delta.getChangeType().equals(ChangeType.DELETE) || !delta.getClassChange().getClassRole().equals(ClassRole.SERVICE)) {
            return useCases;
        }


        // For each restCall if we don't find a match in the new System
        // TODO this technically includes RestCalls that have already been flagged in the past
        for (RestCall restCall : delta.getClassChange().getRestCalls()) {
            if (!findMatch(restCall, newSystem)) {
                UseCase3 useCase3 = new UseCase3();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("RestCall", restCall.getID());
                useCase3.setMetaData(jsonObject);
                useCase3.setOldCommitID(oldSystem.getCommitID());
                useCase3.setNewCommitID(newSystem.getCommitID());
                useCases.add(useCase3);
            }
        }

        return useCases;
    }

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


    public static List<UseCase3> scan2(MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<UseCase3> useCases = new ArrayList<>();

        List<RestCall> allRestCalls = newSystem.getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).flatMap(jClass -> jClass.getRestCalls().stream()).collect(Collectors.toList());


        // For each restCall if we don't find a match in the new System
        // TODO this technically includes RestCalls that have already been flagged in the past
        for (RestCall restCall : allRestCalls) {
            if (!findMatch(restCall, newSystem)) {
                UseCase3 useCase3 = new UseCase3();
                JsonObject jsonObject = new JsonObject();
//                jsonObject.add("RestCall", restCall.toJsonObject());
                useCase3.setMetaData(jsonObject);
                useCase3.setOldCommitID(oldSystem.getCommitID());
                useCase3.setNewCommitID(newSystem.getCommitID());
                useCases.add(useCase3);
            }
        }

        return useCases;
    }
}

