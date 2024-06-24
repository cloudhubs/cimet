package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.Microservice;
import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase1 extends UseCase{
    protected static final String NAME = "Floating call due to endpoint removal (internal)";
    protected static final Scope SCOPE = Scope.ENDPOINT;
    protected static final String DESC = "An endpoint was removed, inter service calls depending on this method are no longer called";
    protected JsonObject metaData;

    private UseCase1() {}

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
    
    public static UseCase1 scan(Endpoint endpoint, MicroserviceSystem microserviceSystem){
        //TODO add implementation 


        UseCase1 useCase1 = new UseCase1();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("Endpoint", endpoint.toJsonObject());
        useCase1.setMetaData(jsonObject);    
        return useCase1;
    }
}
