package edu.university.ecs.lab.metrics.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.metrics.models.metrics.GreedyMicroservice;

import java.util.HashSet;
import java.util.Set;

public class GreedyService {

    private static final int RESTCALL_THRESHOLD = 5;

    public GreedyMicroservice getGreedyMicroservices(NetworkGraph graph) {
        Set<String> getGreedyMicroservices = new HashSet<>();

        for (String microserviceName : graph.getNodes()) {
            int restCallCount = 0;
            for (Edge edge : graph.getEdges()) {
                if (microserviceName.equals(edge.getSource())) {
                    restCallCount++;
                }
            }
            if (restCallCount >= RESTCALL_THRESHOLD) {
                getGreedyMicroservices.add(microserviceName);
            }
        }

        GreedyMicroservice greedyMicroservices = new GreedyMicroservice(getGreedyMicroservices);

        return greedyMicroservices;
    }
}
