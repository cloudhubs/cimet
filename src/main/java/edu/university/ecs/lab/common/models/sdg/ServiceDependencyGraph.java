package edu.university.ecs.lab.common.models.sdg;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Getter;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a service dependency graph for a microservice system.
 */
@Getter
public class ServiceDependencyGraph extends DirectedWeightedMultigraph<Microservice, RestCallEdge>
        implements JsonSerializable, DependencyGraphI<Microservice, RestCallEdge> {
    private final String label;
    private final String timestamp;
    private final boolean directed = true;
    private final boolean multigraph = true;

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(RestCallEdge.class, new RestCallEdgeSerializer());
        gsonBuilder.registerTypeAdapter(Microservice.class, new MicroserviceSerializer());
        Gson gson = gsonBuilder.create();

        String nodesArray = gson.toJson(this.vertexSet());
        String edgeArray = gson.toJson(this.edgeSet());

        jsonObject.addProperty("label", label);
        jsonObject.addProperty("timestamp", timestamp);
        jsonObject.addProperty("directed", directed);
        jsonObject.addProperty("multigraph", multigraph);
        jsonObject.addProperty("nodes", nodesArray);
        jsonObject.addProperty("edges", edgeArray);

        return jsonObject;
    }

    /**
     * Creates the network graph from a given MicroserviceSystem.
     *
     * @param microserviceSystem the microservice system to build the graph from.
     */
    public ServiceDependencyGraph(MicroserviceSystem microserviceSystem) {
        super(RestCallEdge.class);
        this.label = microserviceSystem.getName();
        this.timestamp = microserviceSystem.getCommitID();

        Map<String, Microservice> ms = new HashMap<>();
        List<RestCall> restCalls = new ArrayList<>();
        List<Endpoint> endpoints = new ArrayList<>();

        microserviceSystem.getMicroservices().forEach(microservice -> {
            ms.put(microservice.getName(), microservice);
            this.addVertex(microservice);
            restCalls.addAll(microservice.getRestCalls());
            endpoints.addAll(microservice.getEndpoints());
        });

        List<List<String>> edgesList = new ArrayList<>();

        for (RestCall restCall : restCalls) {
            for (Endpoint endpoint : endpoints) {
                if (RestCall.matchEndpoint(restCall, endpoint)) {
                    edgesList.add(Arrays.asList(restCall.getMicroserviceName(), endpoint.getMicroserviceName(), endpoint.getUrl()));
                }
            }
        }

        edgesList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((edgeData, value) -> {
             RestCallEdge edge = this.addEdge(ms.get(edgeData.get(0)), ms.get(edgeData.get(1)));
             edge.setEndpoint(edgeData.get(2));
             this.setEdgeWeight(edge, value);
         });
    }

    public static class MicroserviceSerializer implements JsonSerializer<Microservice> {
        @Override
        public JsonElement serialize(Microservice microservice, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", microservice.getName());
            return jsonObject;
        }
    }

    public class RestCallEdgeSerializer implements JsonSerializer<RestCallEdge> {
        @Override
        public JsonElement serialize(RestCallEdge edge, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("source", ServiceDependencyGraph.this.getEdgeSource(edge).getName());
            jsonObject.addProperty("target", ServiceDependencyGraph.this.getEdgeTarget(edge).getName());
            jsonObject.addProperty("endpoint", edge.getEndpoint());
            jsonObject.addProperty("weight", ServiceDependencyGraph.this.getEdgeWeight(edge));
            return jsonObject;
        }
    }
}
