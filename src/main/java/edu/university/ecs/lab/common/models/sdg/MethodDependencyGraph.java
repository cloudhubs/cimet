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

@Getter
public class MethodDependencyGraph extends DirectedWeightedPseudograph<Method, DefaultWeightedEdge>
        implements DependencyGraphI<Method, DefaultWeightedEdge> {

    private final String label;
    private final String timestamp;
    private final boolean directed = true;
    private final boolean multigraph = false;

    public MethodDependencyGraph(MicroserviceSystem microserviceSystem) {
        super(DefaultWeightedEdge.class);
        this.label = microserviceSystem.getName();
        this.timestamp = microserviceSystem.getCommitID();

        Map<String, Method> methods = new HashMap<>();
        Set<MethodCall> methodCalls = new HashSet<>();

        microserviceSystem.getMicroservices()
                .forEach(ms -> {
                            methodCalls.addAll(ms.getMethodCalls());
                            ms.getMethods()
                                .forEach(method -> {
                                    this.addVertex(method);
                                    methods.put(method.getMicroserviceName() + method.getClassName() + method.getName(), method);
        });});

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
    public static class MethodSerializer implements JsonSerializer<Method> {
        @Override
        public JsonElement serialize(Method method, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", method.getID());
            return jsonObject;
        }
    }
    public static class EndpointSerializer implements JsonSerializer<Endpoint> {
        @Override
        public JsonElement serialize(Endpoint endpoint, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", endpoint.getID());
            return jsonObject;
        }
    }

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
