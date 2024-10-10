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
    
    /**
     * Represents the name of the graph
     */
    private final String label;
    /**
     * The timestamp of the current Network graph
     * (i.e. the commit ID that the Network graph represents)
     */
    private final String timestamp;
    /**
     * Whether the edges are interpreted as directed
     */
    private final boolean directed = true;
    /**
     * Whether several edges between source and target are allowed
     */
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

        // Add microservices, rest calls, and enpoints to respective lists, add microservice to graph as a vertex
        microserviceSystem.getMicroservices().forEach(microservice -> {
            ms.put(microservice.getName(), microservice);
            this.addVertex(microservice);
            restCalls.addAll(microservice.getRestCalls());
            endpoints.addAll(microservice.getEndpoints());
        });

        List<List<String>> edgesList = new ArrayList<>();

        // Add services with matching rest call/endpoint pairs to edges list
        for (RestCall restCall : restCalls) {
            for (Endpoint endpoint : endpoints) {
                if (RestCall.matchEndpoint(restCall, endpoint)) {
                    edgesList.add(Arrays.asList(restCall.getMicroserviceName(), endpoint.getMicroserviceName(), endpoint.getUrl()));
                }
            }
        }

        // Add edges to map
        edgesList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((edgeData, value) -> {
             RestCallEdge edge = this.addEdge(ms.get(edgeData.get(0)), ms.get(edgeData.get(1)));
             edge.setEndpoint(edgeData.get(2));
             this.setEdgeWeight(edge, value);
         });
    }

    /**
     * Class to serialize a microservice as a json object
     */
    public static class MicroserviceSerializer implements JsonSerializer<Microservice> {
        @Override
        public JsonElement serialize(Microservice microservice, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", microservice.getName());
            return jsonObject;
        }
    }

    /**
     * Class to serialize a rest call as a json object
     */
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
