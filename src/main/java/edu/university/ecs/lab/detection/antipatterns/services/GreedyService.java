package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.sdg.EndpointCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.GreedyMicroservice;

import java.util.HashSet;
import java.util.Set;


/**
 * Service class to identify and manage microservices identified as greedy.
 */
public class GreedyService {

    /**
     * Threshold for the number of REST calls indicating a microservice is greedy.
     */
    private static final int RESTCALL_THRESHOLD = 5;

    /**
     * Retrieves microservices identified as greedy based on REST call threshold.
     *
     * @param graph the network graph to analyze
     * @return a GreedyMicroservice object containing identified greedy microservices
     */
    public GreedyMicroservice getGreedyMicroservices(ServiceDependencyGraph graph) {
        Set<String> getGreedyMicroservices = new HashSet<>();

        for (String microserviceName : graph.getNodes()) {
            int restCallCount = 0;
            for (EndpointCallEdge edge : graph.getEdges()) {
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
