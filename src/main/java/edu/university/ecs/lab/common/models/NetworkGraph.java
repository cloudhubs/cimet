package edu.university.ecs.lab.common.models;

import java.util.List;

public class NetworkGraph {
    private final String label;
    private final boolean directed;
    private final List<String> nodes;
    private final List<Edge> edges;

    public NetworkGraph(String label, boolean directed, List<String> nodes, List<Edge> edges) {
        this.label = label;
        this.directed = directed;
        this.nodes = nodes;
        this.edges = edges;
    }

    public NetworkGraph(String label, List<String> nodes, List<Edge> edges) {
        this.label = label;
        this.directed = true;
        this.nodes = nodes;
        this.edges = edges;
    }
}
