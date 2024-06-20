package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.sdg.Edge;
import edu.university.ecs.lab.common.models.sdg.NetworkGraph;
import edu.university.ecs.lab.detection.antipatterns.models.WrongCuts;

import java.util.*;

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
    public List<WrongCuts> identifyAndReportWrongCuts(NetworkGraph graph) {
        List<Set<String>> wrongCutsList = detectWrongCuts(graph);
        List<WrongCuts> wrongCutsObjects = new ArrayList<>();

        for (Set<String> wrongCut : wrongCutsList) {
            if (wrongCut.size() > 1) { 
                wrongCutsObjects.add(new WrongCuts(wrongCut));
            }
        }

        return wrongCutsObjects;
    }

    /**
     * Detects all clusters of wrongly interconnected services in the given network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of sets, each containing services that are wrongly interconnected (forming a cluster).
     */
    public List<Set<String>> detectWrongCuts(NetworkGraph graph) {
        Map<String, List<String>> adjacencyList = buildAdjacencyList(graph);
        Set<String> visited = new HashSet<>();
        List<Set<String>> wrongCuts = new ArrayList<>();

        for (String node : graph.getNodes()) {
            if (!visited.contains(node)) {
                Set<String> cluster = new HashSet<>();
                dfs(node, adjacencyList, visited, cluster);
                wrongCuts.add(cluster);
            }
        }

        return wrongCuts;
    }

    /**
     * Builds an adjacency list representation of the network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A map where each key is a service and its value is a list of services it directly depends on.
     */
    private Map<String, List<String>> buildAdjacencyList(NetworkGraph graph) {
        Map<String, List<String>> adjacencyList = new HashMap<>();

        for (String node : graph.getNodes()) {
            adjacencyList.put(node, new ArrayList<>());
        }

        for (Edge edge : graph.getEdges()) {
            adjacencyList.get(edge.getSource()).add(edge.getTarget());
        }

        return adjacencyList;
    }

    /**
     * Performs Depth-First Search (DFS) to traverse and collect all nodes in the current cluster of wrong cuts.
     *
     * @param currentNode   The current node being visited.
     * @param adjacencyList The adjacency list representing the network graph.
     * @param visited       Set of visited nodes to avoid revisiting.
     * @param cluster       Set to collect all nodes belonging to the current cluster of wrong cuts.
     */
    private void dfs(String currentNode, Map<String, List<String>> adjacencyList, Set<String> visited, Set<String> cluster) {
        visited.add(currentNode);
        cluster.add(currentNode);

        for (String neighbor : adjacencyList.getOrDefault(currentNode, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacencyList, visited, cluster);
            }
        }
    }
}
