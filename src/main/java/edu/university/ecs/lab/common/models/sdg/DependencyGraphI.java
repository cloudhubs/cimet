package edu.university.ecs.lab.common.models.sdg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import org.jgrapht.Graph;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface DependencyGraphI<V, E> extends Graph<V, E>, JsonSerializable {

    /**
     * Represents the name of the graph
     */
    String getLabel();
    /**
     * The timestamp of the current Network graph
     * (i.e. the commit ID that the Network graph represents)
     */
    String getTimestamp();
    /**
     * Whether the edges are interpreted as directed
     */
    boolean isDirected();
    /**
     * Whether several edges between source and target are allowed
     */
    boolean isMultigraph();

    default Map<V, Set<V>> getAdjacency() {
        return this.vertexSet().stream()
                .collect(Collectors.toMap(
                        vertex -> vertex,
                        this::getAdjacency
                ));
    }

    default Set<V> getAdjacency(V vertex) {
        return this.outgoingEdgesOf(vertex).stream()
                .map(this::getEdgeTarget)
                .collect(Collectors.toSet());
    }


    default JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        String nodesArray = gson.toJson(this.vertexSet());
        String edgeArray = gson.toJson(this.edgeSet());

        jsonObject.addProperty("label", getLabel());
        jsonObject.addProperty("timestamp", getTimestamp());
        jsonObject.addProperty("directed", isDirected());
        jsonObject.addProperty("multigraph", isMultigraph());
        jsonObject.addProperty("nodes", nodesArray);
        jsonObject.addProperty("edges", edgeArray);

        return jsonObject;
    }

}