package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.utils.FlowUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.Data;

@Data
public class UseCase1 extends AbstractUseCase {
    protected static final String TYPE = "UseCase1";
    protected static final String NAME = "Floating call due to endpoint removal (internal)";
    protected static final String DESC = "An endpoint was removed, inter service calls depending on this method are no longer called";
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
    public JsonObject getMetaData() {
        return metaData;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static List<UseCase1> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem){
        List<UseCase1> useCases = new ArrayList<>();

        // Old class for delete, delta class for modify
        JClass jClass = delta.getChangeType().equals(ChangeType.MODIFY) ? delta.getClassChange() : oldSystem.findClass(delta.getOldPath());

        if(delta.getChangeType().equals(ChangeType.ADD) || !jClass.getClassRole().equals(ClassRole.CONTROLLER)) {
            return useCases;
        }

        List<Flow> flows = FlowUtils.buildFlows(oldSystem);
        Endpoint endpointMatch = null;
        for(Flow flow : flows){
            if(flow.getController() != null && flow.getController().getPath().equals(delta.getOldPath())){
                for(Endpoint endpoint : jClass.getEndpoints()) {
                    if(endpoint.getName().equals(flow.getControllerMethod().getName())){
                        endpointMatch = endpoint;
                    }
                }
                // If we find no match or its a delete, we have removed this method
                if((Objects.nonNull(endpointMatch) || delta.getChangeType().equals(ChangeType.DELETE)) && flow.getService() != null){
                    for(RestCall restCall : flow.getService().getRestCalls()) {
                        // If this restCall is called in the severed flow
                        if(flow.getServiceMethod() != null && restCall.getCalledFrom().equals(flow.getServiceMethod().getName())){
                            UseCase1 useCase1 = new UseCase1();
                            JsonObject jsonObject = new JsonObject();
//                            jsonObject.add("RestCall", restCall.toJsonObject());
//                            jsonObject.add("Endpoint", endpointMatch == null ? new JsonObject() : endpointMatch.toJsonObject());
                            useCase1.setMetaData(jsonObject);
                            useCase1.setOldCommitID(oldSystem.getCommitID());
                            useCase1.setNewCommitID(newSystem.getCommitID());
                            useCases.add(useCase1);
                        }
                    }
                }

                endpointMatch = null;
            }


        }

        return useCases;
    }

    public static List<UseCase1> scan2(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem){
        List<UseCase1> useCases = new ArrayList<>();

        List<Flow> flows = FlowUtils.buildFlows(oldSystem);
        List<RestCall> allRestCalls = newSystem.getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).flatMap(jClass -> jClass.getRestCalls().stream()).collect(Collectors.toList());

        for(RestCall restCall : allRestCalls) {
            outer:
            {
                for (Flow flow : flows) {
                    if (Objects.nonNull(flow.getRepositoryMethodCall()) && flow.getRepositoryMethodCall().equals(restCall)) {
                        break outer;
                    }
                }

                // If the restCall is not in any flows (flows start at controller)
                UseCase1 useCase1 = new UseCase1();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("RestCall", restCall.getID());
                useCase1.setMetaData(jsonObject);
                useCase1.setOldCommitID(oldSystem.getCommitID());
                useCase1.setNewCommitID(newSystem.getCommitID());
                useCases.add(useCase1);

            }
        }

        return useCases;
    }
}
