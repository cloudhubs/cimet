package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.GreedyMicroservice;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * Service class to identify and manage microservices identified as greedy.
 */
public class GreedyService {

    /**
     * Threshold for the number of REST calls indicating a microservice is greedy.
     */
    private final int RESTCALL_THRESHOLD;

    /**
     * Retrieves microservices identified as greedy based on REST call threshold.
     *
     * @param graph the network graph to analyze
     * @return a GreedyMicroservice object containing identified greedy microservices
     */
    public GreedyMicroservice getGreedyMicroservices(ServiceDependencyGraph graph) {

        Set<String> getGreedyMicroservices = graph.vertexSet().stream().filter(vertex -> graph.outgoingEdgesOf(vertex).stream()
                .map(graph::getEdgeWeight).mapToDouble(Double::doubleValue).sum() >= (double) RESTCALL_THRESHOLD).collect(Collectors.toSet());

        return new GreedyMicroservice(getGreedyMicroservices);
    }

    public GreedyService() {
        RESTCALL_THRESHOLD = 6;
    }

    public GreedyService(int RESTCALL_THRESHOLD) {
        this.RESTCALL_THRESHOLD = RESTCALL_THRESHOLD;
    }

}
