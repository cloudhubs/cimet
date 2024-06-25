package edu.university.ecs.lab.detection.antipatterns.services;

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
        Map<String, Set<String>> adjacencyList = graph.getAdjacency();

        Set<String> globalVisited = new HashSet<>();

        for (String node : graph.vertexSet()) {
            if (!globalVisited.contains(node)) {
                dfs(node, new ArrayList<>(), allChains, adjacencyList, globalVisited);
            }
        }

        return allChains;
    }

    /**
     * Depth-first search (DFS) to explore and detect service chains starting from currentNode.
     *
     * @param currentNode the current node being visited
     * @param currentPath the current path of nodes being explored
     * @param allChains   list to store detected service chains
     * @param adjacencyList adjacency list representation of the network graph
     * @param globalVisited set of globally visited nodes
     */
    private void dfs(String currentNode, List<String> currentPath, List<ServiceChain> allChains, Map<String, Set<String>> adjacencyList, Set<String> globalVisited) {
        if (globalVisited.contains(currentNode)) {
            return;
        }

        currentPath.add(currentNode);
        globalVisited.add(currentNode);

        Set<String> neighbors = adjacencyList.get(currentNode);
        if (neighbors != null && !neighbors.isEmpty()) {
            for (String neighbor : neighbors) {
                if (!globalVisited.contains(neighbor)) {
                    dfs(neighbor, currentPath, allChains, adjacencyList, globalVisited);
                }
            }
        }

        if (currentPath.size() > 1) {
            allChains.add(new ServiceChain(new ArrayList<>(currentPath)));
        }

        currentPath.remove(currentPath.size() - 1);
    }
}
