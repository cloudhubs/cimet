package edu.university.ecs.lab.metrics.models.metrics;

import java.util.*;

import lombok.Data;

@Data
public class WrongCuts {
    private Set<String> wrongCuts = new HashSet<>();

    public WrongCuts(Set<String> wrongCuts){
        this.wrongCuts = wrongCuts;
    }
}
