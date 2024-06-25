package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
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
    public List<ServiceChain> getServiceChains(NetworkGraph graph) {
        List<ServiceChain> allChains = new ArrayList<>();
        Map<String, List<String>> adjacencyList = buildAdjacencyList(graph);

        Set<String> globalVisited = new HashSet<>();

        for (String node : graph.getNodes()) {
            if (!globalVisited.contains(node)) {
                dfs(node, new ArrayList<>(), allChains, adjacencyList, globalVisited);
            }
        }

        return allChains;
    }

    /**
     * Builds an adjacency list representation of the network graph.
     *
     * @param graph the network graph
     * @return adjacency list mapping each node to its list of neighboring nodes
     */
    private Map<String, List<String>> buildAdjacencyList(NetworkGraph graph) {
        Map<String, List<String>> adjacencyList = new HashMap<>();
        for (String node : graph.getNodes()) {
            adjacencyList.put(node, new ArrayList<>());
        }
        for (Edge edge : graph.getEdges()) {
            List<String> neighbors = adjacencyList.get(edge.getSource());
            if (!neighbors.contains(edge.getTarget())) {
                neighbors.add(edge.getTarget());
            }
        }
        return adjacencyList;
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
    private void dfs(String currentNode, List<String> currentPath, List<ServiceChain> allChains, Map<String, List<String>> adjacencyList, Set<String> globalVisited) {
        if (globalVisited.contains(currentNode)) {
            return;
        }

        currentPath.add(currentNode);
        globalVisited.add(currentNode);

        List<String> neighbors = adjacencyList.get(currentNode);
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
