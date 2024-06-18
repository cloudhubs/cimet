package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;

import java.util.*;

public class ServiceChainService {

    public List<ServiceChain> getServiceChains(NetworkGraph graph) {
        List<ServiceChain> allChains = new ArrayList<>();
        Map<String, List<String>> adjacencyList = buildAdjacencyList(graph);

        for (String node : graph.getNodes()) {
            Set<String> visited = new HashSet<>();
            dfs(node, new ArrayList<>(), allChains, adjacencyList, visited);
        }

        return allChains;
    }

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

    private void dfs(String currentNode, List<String> currentPath, List<ServiceChain> allChains, Map<String, List<String>> adjacencyList, Set<String> visited) {
        visited.add(currentNode);
        currentPath.add(currentNode);

        List<String> neighbors = adjacencyList.get(currentNode);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, currentPath, allChains, adjacencyList, visited);
                }
            }
        }

        // Check if the current node is a leaf node (no outgoing edges)
        if (neighbors == null || neighbors.isEmpty()) {
            allChains.add(new ServiceChain(new ArrayList<>(currentPath)));
        }

        // Backtrack
        currentPath.remove(currentPath.size() - 1);
    }
}
