package edu.university.ecs.lab.common.models.sdg;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Getter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a dependency graph for service methods
 */
@Getter
public class MethodDependencyGraph extends DirectedWeightedPseudograph<Method, DefaultWeightedEdge>
        implements DependencyGraphI<Method, DefaultWeightedEdge> {

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
    private final boolean multigraph = false;

    /**
     * Create a new method dependency graph from a given microservice system
     * 
     * @param microserviceSystem microservice system from which to derive method dependency graph
     */
    public MethodDependencyGraph(MicroserviceSystem microserviceSystem) {
        super(DefaultWeightedEdge.class);
        this.label = microserviceSystem.getName();
        this.timestamp = microserviceSystem.getCommitID();

        // Map of method details to method objects
        Map<String, Method> methods = new HashMap<>();

        // Set of all method calls
        Set<MethodCall> methodCalls = new HashSet<>();

        // Add method calls to set, map methods to microservice and class names
        microserviceSystem.getMicroservices()
                .forEach(ms -> {
                            methodCalls.addAll(ms.getMethodCalls());
                            ms.getMethods()
                                .forEach(method -> {
                                    this.addVertex(method);
                                    methods.put(method.getMicroserviceName() + method.getClassName() + method.getName(), method);
        });});

        // Add method/method call pairs to method dependency graph
        methods.values().forEach(targetMethod -> methodCalls.forEach(methodCall ->
                {
                    Method sourceMethod = methods.get(methodCall.getMicroserviceName()+methodCall.getClassName()+methodCall.getCalledFrom());
                    if (sourceMethod == null) {return; }
                    if (methodCall instanceof RestCall) {
                        if (targetMethod instanceof Endpoint) {
                            if (RestCall.matchEndpoint((RestCall) methodCall, (Endpoint) targetMethod)) {
                                this.addUpdateEdge(sourceMethod, targetMethod);
                            }
                        }
                    } else {
                        if (MethodCall.matchMethod(methodCall, targetMethod)) {
                            this.addUpdateEdge(sourceMethod, targetMethod);
                        }
                    }
                }
        ));

    }

    /**
     * Method to add and edge or update its weight if it already exists
     * 
     * @param source method making the call
     * @param target method being called
     */
    private void addUpdateEdge(Method source, Method target) {
        DefaultWeightedEdge edge = this.getEdge(source, target);
        if (edge == null) {
            this.addEdge(source, target);
        } else {
            this.setEdgeWeight(edge, this.getEdgeWeight(edge) + 1.0);
        }
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DefaultWeightedEdge.class, new MethodDependencyGraph.MethodCallEdgeSerializer());
        gsonBuilder.registerTypeAdapter(Method.class, new MethodDependencyGraph.MethodSerializer());
        gsonBuilder.registerTypeAdapter(Endpoint.class, new MethodDependencyGraph.EndpointSerializer());
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
     * Class to serialize a method as a json object
     */
    public static class MethodSerializer implements JsonSerializer<Method> {
        @Override
        public JsonElement serialize(Method method, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", method.getID());
            return jsonObject;
        }
    }

    /**
     * Class to serialize an endpoint as a json object
     */
    public static class EndpointSerializer implements JsonSerializer<Endpoint> {
        @Override
        public JsonElement serialize(Endpoint endpoint, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", endpoint.getID());
            return jsonObject;
        }
    }

    /**
     * Class to serialize an endpoint as a json object
     */
    public class MethodCallEdgeSerializer implements JsonSerializer<DefaultWeightedEdge> {
        @Override
        public JsonElement serialize(DefaultWeightedEdge edge, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("source", MethodDependencyGraph.this.getEdgeSource(edge).getID());
            jsonObject.addProperty("target", MethodDependencyGraph.this.getEdgeTarget(edge).getID());
            jsonObject.addProperty("weight", MethodDependencyGraph.this.getEdgeWeight(edge));
            return jsonObject;
        }
    }
}
