package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for identifying and managing hub-like microservices in a network graph.
 */
public class HubLikeService {
    /**
     * Threshold for the number of REST calls indicating a microservice is hub-like.
     */
    protected static final int DEFAULT_RESTCALL_THRESHOLD = GreedyService.DEFAULT_RESTCALL_THRESHOLD;
    private final int RESTCALL_THRESHOLD;

    /**
     * Retrieves microservices identified as hub-like based on REST call threshold.
     *
     * @param graph the network graph to analyze
     * @return a HubLikeMicroservice object containing identified hub-like microservices
     */
    public HubLikeMicroservice getHubLikeMicroservice(ServiceDependencyGraph graph) {
        
        // Filter vertices (microservices) to only those with a sum of incoming edge weights 
        // greater than or equal to the set restcall threshhold
        List<String> getHubMicroservices = graph.vertexSet().stream().filter(vertex -> graph.incomingEdgesOf(vertex).stream()
                .map(graph::getEdgeWeight).mapToDouble(Double::doubleValue).sum() >= (double) RESTCALL_THRESHOLD)
                .map(Microservice::getName).collect(Collectors.toList());

        return new HubLikeMicroservice(getHubMicroservices);
    }

    public HubLikeService() {
        RESTCALL_THRESHOLD = DEFAULT_RESTCALL_THRESHOLD;
    }

    public HubLikeService(int RESTCALL_THRESHOLD) {
        this.RESTCALL_THRESHOLD = RESTCALL_THRESHOLD;
    }
}
