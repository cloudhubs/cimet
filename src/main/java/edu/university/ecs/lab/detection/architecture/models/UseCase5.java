package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.Microservice;
import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.common.models.RestCall;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase5 extends UseCase{
    protected static final String NAME = "Bad Call";
    protected static final Scope SCOPE = Scope.REST_CALL;
    protected static final String DESC = "A rest call was modified and now incorrectly references an endpoint for one more more resaons (Return Type, HttpMethod, Parameters)";
    protected JsonObject metaData;


    private UseCase5() {}

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

    public static UseCase5 scan(RestCall restCall, MicroserviceSystem microserviceSystem){
        //TODO add implementation

        UseCase5 useCase5 = new UseCase5();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("Rest Call", restCall.toJsonObject());
        useCase5.setMetaData(jsonObject);    
        return useCase5;
    }
}
