package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class Edge {
    private String source;
    private String target;
    private String endpoint;
    private int weight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target) && Objects.equals(endpoint, edge.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, endpoint);
    }
}
