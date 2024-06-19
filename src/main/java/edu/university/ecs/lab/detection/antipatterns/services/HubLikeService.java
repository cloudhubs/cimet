package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for identifying and managing hub-like microservices in a network graph.
 */
public class HubLikeService {
    /**
     * Threshold for the number of REST calls indicating a microservice is hub-like.
     */
    private static final int RESTCALL_THRESHOLD = 5;

    /**
     * Retrieves microservices identified as hub-like based on REST call threshold.
     *
     * @param graph the network graph to analyze
     * @return a HubLikeMicroservice object containing identified hub-like microservices
     */
    public HubLikeMicroservice getHubLikeMicroservice(NetworkGraph graph) {
        Set<String> getHubMircoservice = new HashSet<>();

        for (String microserviceName : graph.getNodes()) {
            int restCallCount = 0;
            for (Edge edge : graph.getEdges()) {
                if (microserviceName.equals(edge.getTarget())) {
                    restCallCount++;
                }
            }
            if (restCallCount >= RESTCALL_THRESHOLD) {
                getHubMircoservice.add(microserviceName);
            }
        }

        HubLikeMicroservice hublLikeMicroservice = new HubLikeMicroservice(getHubMircoservice);

        return hublLikeMicroservice;
    }
}
