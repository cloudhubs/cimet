package edu.university.ecs.lab.common.models.sdg;

import com.google.gson.*;

import java.lang.reflect.Type;

class EdgeSerializer implements JsonSerializer<EndpointCallEdge> {
    private final ServiceDependencyGraph graph;

    public EdgeSerializer(ServiceDependencyGraph graph) {
        this.graph = graph;
    }

    @Override
    public JsonElement serialize(EndpointCallEdge edge, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("source", graph.getEdgeSource(edge));
        jsonObject.addProperty("target", graph.getEdgeTarget(edge));
        jsonObject.addProperty("endpoint", edge.getEndpoint());
        jsonObject.addProperty("weight", graph.getEdgeWeight(edge));
        return jsonObject;
    }
}