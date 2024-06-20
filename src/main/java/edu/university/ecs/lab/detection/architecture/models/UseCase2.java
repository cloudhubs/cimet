package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
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
    
    public static List<UseCase2> scan(Delta delta, JClass oldClass, MicroserviceSystem newSystem) {
        List<UseCase2> useCases = new ArrayList<>();

        if (!delta.getChangeType().equals(ChangeType.DELETE) || !delta.getClassChange().getClassRole().equals(ClassRole.CONTROLLER)) {
            return useCases;
        }

        for (Endpoint endpoint : oldClass.getEndpoints()) {
            if (!existsInSystem(endpoint, newSystem)) {
                UseCase2 useCase2 = new UseCase2();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("Endpoint", endpoint.toJsonObject());
                useCase2.setMetaData(jsonObject);
                useCases.add(useCase2);
            }
        }
        return useCases;
    }

    private static boolean existsInSystem(Endpoint endpoint, MicroserviceSystem newSystem) {
        for (Microservice microservice : newSystem.getMicroservices()) {
            for (JClass controller : microservice.getControllers()) {
                for (Endpoint endP : controller.getEndpoints()) {
                    if (endpoint.equals(endP)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
