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

/**
 * Architectural Rule 20 Class: Hublike Service
 */
@Data
public class AR20 extends AbstractAR {

    /**
     * Architectural rule 20 details
     */
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

    /**
     * Scan and compare old microservice system and new microservice system to identify hublike services
     * 
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list of hublike microservices with a sum of incoming edges greater than or equal to 4
     */
    public static List<AR20> scan(MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<AR20> archRules = new ArrayList<>();

        // Create microservice network graph
        ServiceDependencyGraph graph = new ServiceDependencyGraph(newSystem);
        
        // Get list of microservices with a sum of incoming edges greater than or equal to 4 (hublike)
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
