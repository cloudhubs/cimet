package edu.university.ecs.lab.detection.antipatterns.models;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CyclicDependency {
    private List<String> cycle = new ArrayList<>();

    public CyclicDependency(List<String> cycle) {
        this.cycle = cycle;
    }
}