package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AR20 extends AbstractAR {
    protected static final String TYPE = "Architectural Rule 20";
    protected static final String NAME = "Hublike Service";
    protected static final String DESC = "";
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

    public static List<AR20> scan(MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<AR20> archRules = new ArrayList<>();

        ServiceDependencyGraph graph = new ServiceDependencyGraph(newSystem);
        Set<String> getHubMicroservices = graph.vertexSet().stream().filter(vertex -> graph.incomingEdgesOf(vertex).stream()
                .map(graph::getEdgeWeight).mapToDouble(Double::doubleValue).sum() >= (double) 4)
                .map(Microservice::getName).collect(Collectors.toSet());

        getHubMicroservices.forEach(s -> {
            AR20 archRule20 = new AR20();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Microservice", s);
            archRule20.setMetaData(jsonObject);
            archRule20.setOldCommitID(oldSystem.getCommitID());
            archRule20.setNewCommitID(newSystem.getCommitID());
            archRules.add(archRule20);
        });

        return archRules;

    }
}
