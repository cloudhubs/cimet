package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Edge {
    private final String source;
    private final String target;
    private final int weight;

}
