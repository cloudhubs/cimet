package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.ir.RestCall;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase4 extends UseCase{
    protected static final String NAME = "Floating endpoint due to last call removal";
    protected static final Scope SCOPE = Scope.REST_CALL;
    protected static final String DESC = "Any rest calls referencing an endpoint are now gone. This endpoint is now unsued by any other microservice";
    protected JsonObject metaData;


    private UseCase4() {}

    @Override
    public List<? extends UseCase> checkUseCase() {
        ArrayList<UseCase3> useCases = new ArrayList<>();

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

    public static UseCase4 scan(RestCall restCall, MicroserviceSystem microserviceSystem){
        List<Endpoint> endpointsNoRC = new ArrayList<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            for (JClass controller : microservice.getControllers()) {
                for(JClass service : microservice.getServices()){
                    for(Endpoint endpoint: controller.getEndpoints()){
                        outer: {
                            for (RestCall restcall : service.getRestCalls()){
                                if(RestCall.matchEndpoint(restcall, endpoint)){
                                    break outer;
                                }
                            }
                            endpointsNoRC.add(endpoint);
                        }
                    }
                }
            }
        }

        for (Endpoint endpoint : endpointsNoRC) {
            if (RestCall.matchEndpoint(restCall, endpoint)) {
                UseCase4 useCase4 = new UseCase4();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("Rest Call", restCall.toJsonObject());
                useCase4.setMetaData(jsonObject);
                return useCase4;
            }
        }

        return null;
    }
}
