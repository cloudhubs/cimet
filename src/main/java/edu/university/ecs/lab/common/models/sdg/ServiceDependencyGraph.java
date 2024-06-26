package edu.university.ecs.lab.common.models.sdg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a service dependency graph for a microservice system.
 */
public class ServiceDependencyGraph extends DirectedWeightedMultigraph<String, EndpointCallEdge> implements JsonSerializable {
    /**
     * Represents the name of the network graph
     */
    private final String label;
    /**
     * Holds the timestamp of the current Network graph 
     * (i.e. the commit ID that the Network graph represents)
     */
    private final String timestamp;
    /**
     * Whether the edges are interpreted as directed (default true)
     */
    private final boolean directed = true;
    /**
     * Whether several edges between source and target are allowed, distinguished by the endpoint
     */
    private final boolean multigraph = true;

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EndpointCallEdge.class, new EdgeSerializer(this));
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
        super(EndpointCallEdge.class);
        this.label = microserviceSystem.getName();
        this.timestamp = microserviceSystem.getCommitID();

        List<RestCall> restCalls = new ArrayList<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            for (JClass service : microservice.getServices()) {
                restCalls.addAll(service.getRestCalls());
            }
        }

        List<Endpoint> endpoints = new ArrayList<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            for (JClass controller : microservice.getControllers()) {
                endpoints.addAll(controller.getEndpoints());
            }
        }

        List<List<String>> edgesList = new ArrayList<>();

        for (RestCall restCall : restCalls) {
            for (Endpoint endpoint : endpoints) {
                if (RestCall.matchEndpoint(restCall, endpoint)) {
                    edgesList.add(Arrays.asList(restCall.getMicroserviceName(), endpoint.getMicroserviceName(), endpoint.getUrl()));
                    this.addVertex(endpoint.getMicroserviceName());
                    this.addVertex(restCall.getMicroserviceName());
                }
            }
        }

        edgesList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((edgeData, value) -> {
             EndpointCallEdge edge = this.addEdge(edgeData.get(0), edgeData.get(1));
             edge.setEndpoint(edgeData.get(2));
             this.setEdgeWeight(edge, value);
         });
    }

    public Map<String, Set<String>> getAdjacency() {
        return this.vertexSet().stream()
                .collect(Collectors.toMap(
                        vertex -> vertex,
                        vertex -> this.outgoingEdgesOf(vertex).stream()
                                .map(this::getEdgeTarget)
                                .collect(Collectors.toSet())
                ));
    }
}
