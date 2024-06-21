package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.sdg.EndpointCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;

import java.util.*;

/**
 * Service class for detecting and managing service chains in a network graph.
 */
public class ServiceChainService {

    /**
     * Retrieves all service chains from the given network graph.
     *
     * @param graph the network graph to analyze
     * @return a list of ServiceChain objects representing detected service chains
     */
    public List<ServiceChain> getServiceChains(ServiceDependencyGraph graph) {
        List<ServiceChain> allChains = new ArrayList<>();
        Map<String, List<String>> adjacencyList = graph.getAdjacency();

        for (String node : graph.vertexSet()) {
            Set<String> visited = new HashSet<>();
            dfs(node, new ArrayList<>(), allChains, adjacencyList, visited);
        }

        // Filter out single-service chains
        allChains.removeIf(chain -> chain.getChain().size() <= 1);

        return allChains;
    }

    /**
     * Depth-first search (DFS) to explore and detect service chains starting from currentNode.
     *
     * @param currentNode the current node being visited
     * @param currentPath the current path of nodes being explored
     * @param allChains   list to store detected service chains
     * @param adjacencyList adjacency list representation of the network graph
     * @param visited     set of visited nodes
     */
    private void dfs(String currentNode, List<String> currentPath, List<ServiceChain> allChains, Map<String, List<String>> adjacencyList, Set<String> visited) {
        visited.add(currentNode);
        currentPath.add(currentNode);

        List<String> neighbors = adjacencyList.get(currentNode);
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, currentPath, allChains, adjacencyList, visited);
            }
        }

        if (neighbors.isEmpty()) {
            allChains.add(new ServiceChain(new ArrayList<>(currentPath)));
        }

        // Backtrack
        currentPath.remove(currentPath.size() - 1);
        visited.remove(currentNode);
    }
}
