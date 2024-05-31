package edu.university.ecs.lab.common.models;

public class Edge {
    private final String source;
    private final String target;
    private final int weight;

    public Edge(String source, String target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }
}
