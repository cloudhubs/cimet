package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase2 extends UseCase{
    protected static final String NAME = "Floating call due to endpoint removal (external)";
    protected static final Scope SCOPE = Scope.ENDPOINT;
    protected static final String DESC = "An endpoint was removed and calls now reference an endpoint no longer in existence.";
    protected JsonObject metaData;

    private UseCase2() {}

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
    
    public static UseCase2 scan(Endpoint endpoint, MicroserviceSystem microserviceSystem){
        for (Microservice microservice : microserviceSystem.getMicroservices()){
            for(JClass controller : microservice.getControllers()){
                for (Endpoint endP : controller.getEndpoints()){
                    if (endpoint.equals(endP)){
                        return null;
                    }
                }
            }
        }

        UseCase2 useCase2 = new UseCase2();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("Endpoint", endpoint.toJsonObject());
        useCase2.setMetaData(jsonObject);    
        return useCase2;
    }
}
