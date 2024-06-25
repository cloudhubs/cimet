package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.ir.RestCall;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase4 extends UseCase {

    protected static final String NAME = "Floating endpoint due to last call removal";
    protected static final Scope SCOPE = Scope.REST_CALL;
    protected static final String DESC = "Any rest calls referencing an endpoint are now gone. This endpoint is now unused by any other microservice";
    protected JsonObject metaData;

    private UseCase4() {}

    @Override
    public List<? extends UseCase> checkUseCase() {
        // To be implemented if needed
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public Scope getScope() {
        return SCOPE;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    public static List<UseCase4> scan(Delta delta, MicroserviceSystem microserviceSystemOld, MicroserviceSystem microserviceSystemNew) {
        List<UseCase4> useCases = new ArrayList<>();

        if (!(delta.getChangeType().equals(ChangeType.MODIFY) || delta.getChangeType().equals(ChangeType.DELETE))
                || !delta.getClassChange().getClassRole().equals(ClassRole.SERVICE)) {
            return useCases;
        }

        List<Endpoint> endpointsNoRC = collectEndpointsNoRC(microserviceSystemNew);

        if (delta.getChangeType().equals(ChangeType.MODIFY)) {
            JClass oldClass = microserviceSystemOld.findClass(delta.getNewPath());
            List<RestCall> modifiedRestCalls = collectModifiedRestCalls(delta, oldClass);

            for (RestCall restCall : modifiedRestCalls) {
                for (Endpoint endpoint : endpointsNoRC) {
                    if (RestCall.matchEndpoint(restCall, endpoint)) {
                        UseCase4 useCase4 = new UseCase4();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.add("Rest Call", restCall.toJsonObject());
                        useCase4.setMetaData(jsonObject);
                        useCases.add(useCase4);
                    }
                }
            }
        } else if (delta.getChangeType().equals(ChangeType.DELETE)) {
            JClass oldClass = microserviceSystemOld.findClass(delta.getOldPath());
            Set<RestCall> restCalls = oldClass.getRestCalls();

            for (RestCall restCall : restCalls) {
                for (Endpoint endpoint : endpointsNoRC) {
                    if (RestCall.matchEndpoint(restCall, endpoint)) {
                        UseCase4 useCase4 = new UseCase4();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.add("Rest Call", restCall.toJsonObject());
                        useCase4.setMetaData(jsonObject);
                        useCases.add(useCase4);
                    }
                }
            }
        }

        return useCases;
    }

    private static List<RestCall> collectModifiedRestCalls(Delta d, JClass oldClass){
        List<RestCall> modifiedRestCalls = new ArrayList<>();
        for(RestCall restCallNew: d.getClassChange().getRestCalls()){
            outer: {
                for(RestCall restCallOld: oldClass.getRestCalls()){
                    if(restCallOld.equals(restCallNew)){
                        break outer;
                    }
                }
                modifiedRestCalls.add(restCallNew);
            }
        }

        return modifiedRestCalls;
    }

    private static List<Endpoint> collectEndpointsNoRC(MicroserviceSystem microserviceSystem) {
        List<Endpoint> endpointsNoRC = new ArrayList<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            for (JClass controller : microservice.getControllers()) {
                for (JClass service : microservice.getServices()) {
                    for (Endpoint endpoint : controller.getEndpoints()) {
                        boolean hasRestCall = false;
                        for (RestCall restCall : service.getRestCalls()) {
                            if (RestCall.matchEndpoint(restCall, endpoint)) {
                                hasRestCall = true;
                                break;
                            }
                        }
                        if (!hasRestCall) {
                            endpointsNoRC.add(endpoint);
                        }
                    }
                }
            }
        }

        return endpointsNoRC;
    }
}
