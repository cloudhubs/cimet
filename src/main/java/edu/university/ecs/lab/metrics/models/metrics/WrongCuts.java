package edu.university.ecs.lab.metrics.models.metrics;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class WrongCuts {
    private Set<String> wrongCuts = new HashSet<>();

    public WrongCuts(Set<String> wrongCuts) {
        this.wrongCuts = wrongCuts;
    }
}
