package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice;

import java.util.HashSet;
import java.util.Set;

public class HubLikeService {
    private static final int RESTCALL_THRESHOLD = 5;

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
