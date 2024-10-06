package edu.university.ecs.lab.common.models.sdg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import org.jgrapht.Graph;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an object dependency graph with dependent vertices connected by edges
 */
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

    /**
     * Method to get adjacency list of the entire graph
     * 
     * @return adjacency list of entire graph
     */
    default Map<V, Set<V>> getAdjacency() {
        return this.vertexSet().stream()
                .collect(Collectors.toMap(
                        vertex -> vertex,
                        this::getAdjacency
                ));
    }

    /**
     * Method to get addjacency list of a given vertex
     * 
     * @param vertex vertex to get adjacency list of
     * @return adjacency list for given vertex
     */
    default Set<V> getAdjacency(V vertex) {
        return this.outgoingEdgesOf(vertex).stream()
                .map(this::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
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