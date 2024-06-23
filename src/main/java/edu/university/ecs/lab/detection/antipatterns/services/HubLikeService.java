package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for identifying and managing hub-like microservices in a network graph.
 */
public class HubLikeService {
    /**
     * Threshold for the number of REST calls indicating a microservice is hub-like.
     */
    private final int RESTCALL_THRESHOLD;

    /**
     * Retrieves microservices identified as hub-like based on REST call threshold.
     *
     * @param graph the network graph to analyze
     * @return a HubLikeMicroservice object containing identified hub-like microservices
     */
    public HubLikeMicroservice getHubLikeMicroservice(ServiceDependencyGraph graph) {
        Set<String> getHubMicroservices = graph.vertexSet().stream().filter(vertex -> graph.inDegreeOf(vertex) >= RESTCALL_THRESHOLD).collect(Collectors.toSet());

        return new HubLikeMicroservice(getHubMicroservices);
    }

    public HubLikeService() {
        RESTCALL_THRESHOLD = 6;
    }

    public HubLikeService(int RESTCALL_THRESHOLD) {
        this.RESTCALL_THRESHOLD = RESTCALL_THRESHOLD;
    }
}
