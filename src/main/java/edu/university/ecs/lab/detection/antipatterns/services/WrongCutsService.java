package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.WrongCuts;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for identifying and reporting clusters of wrongly interconnected services (Wrong Cuts)
 * within a microservice network graph.
 */
public class WrongCutsService {
    /**
     * Identifies and reports clusters of wrongly interconnected services based on the provided network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of {@link WrongCuts} objects, each representing a cluster of wrongly interconnected services.
     */
    public List<WrongCuts> identifyAndReportWrongCuts(ServiceDependencyGraph graph) {
        List<Set<String>> wrongCutsList = detectWrongCuts(graph);

        return wrongCutsList.stream().filter(wrongCut -> wrongCut.size() > 1).map(WrongCuts::new)
                .collect(Collectors.toList());
    }

    /**
     * Detects all clusters of wrongly interconnected services in the given network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of sets, each containing services that are wrongly interconnected (forming a cluster).
     */
    public List<Set<String>> detectWrongCuts(ServiceDependencyGraph graph) {
        Map<String, Set<String>> adjacencyList = graph.getAdjacency();
        Set<String> visited = new HashSet<>();
        List<Set<String>> wrongCuts = new ArrayList<>();

        for (String node : graph.vertexSet()) {
            if (!visited.contains(node)) {
                Set<String> cluster = new HashSet<>();
                dfs(node, adjacencyList, visited, cluster);
                wrongCuts.add(cluster);
            }
        }

        return wrongCuts;
    }

    /**
     * Performs Depth-First Search (DFS) to traverse and collect all nodes in the current cluster of wrong cuts.
     *
     * @param currentNode   The current node being visited.
     * @param adjacencyList The adjacency list representing the network graph.
     * @param visited       Set of visited nodes to avoid revisiting.
     * @param cluster       Set to collect all nodes belonging to the current cluster of wrong cuts.
     */
    private void dfs(String currentNode, Map<String, Set<String>> adjacencyList, Set<String> visited, Set<String> cluster) {
        visited.add(currentNode);
        cluster.add(currentNode);

        for (String neighbor : adjacencyList.get(currentNode)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacencyList, visited, cluster);
            }
        }
    }
}
