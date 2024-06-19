package edu.university.ecs.lab.detection.antipatterns.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceChain {
    private List<String> chain = new ArrayList<>();

    public ServiceChain(List<String> sequence) {
        this.chain = sequence;
    }
}