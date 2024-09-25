package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;

import java.util.*;

/**
 * ServiceChainMSLevelService detects service chains in a microservice architecture using DFS.
 */
public class ServiceChainMSLevelService {

    /**
     * Default chain length to consider as an anti-pattern.
     */
    protected static final int DEFAULT_CHAIN_LENGTH = 3;
    private final int CHAIN_LENGTH;
    private List<List<String>> allChains = new ArrayList<>();
    private ServiceDependencyGraph graph = null;
    private List<String> currentPath = new ArrayList<>();
    private boolean hasCycle;  // Flag to indicate if a cycle has been detected in the current path

    /**
     * Constructs the service with the default chain length of 3.
     */
    public ServiceChainMSLevelService() {
        this.CHAIN_LENGTH = DEFAULT_CHAIN_LENGTH;
    }

    /**
     * Constructs the service with a specified chain length.
     *
     * @param CHAIN_LENGTH the length of the chain to consider an anti-pattern.
     */
    public ServiceChainMSLevelService(int CHAIN_LENGTH) {
        this.CHAIN_LENGTH = CHAIN_LENGTH;
    }

    /**
     * Retrieves all service chains from the given network graph.
     *
     * @param graph the network graph to analyze.
     * @return a ServiceChain object representing all detected service chains.
     */
    public ServiceChain getServiceChains(ServiceDependencyGraph graph) {
        allChains = new ArrayList<>();
        this.graph = graph;

        // Traverse every node in the graph, starting a DFS from each node
        for (Microservice node : graph.vertexSet()) {
            currentPath = new ArrayList<>();
            hasCycle = false;  // Reset the cycle detection flag for each DFS call
            dfs(node, new HashSet<>());  // Pass a new empty set for each DFS path to detect cycles
        }

        // Return the detected service chains wrapped in a ServiceChain object.
        return new ServiceChain(allChains);
    }

    /**
     * Depth-first search (DFS) to explore and detect service chains starting from the currentNode.
     * Detects cycles by ensuring that the current node is not already in the current DFS path.
     *
     * @param currentNode the current node being visited in the DFS.
     * @param pathVisited a set to track nodes visited in the current DFS path (for cycle detection).
     */
    private void dfs(Microservice currentNode, Set<Microservice> pathVisited) {
        // If the current node is in the current DFS path, there's a cycle
        if (pathVisited.contains(currentNode)) {
            hasCycle = true;  // Mark that a cycle was detected in this path
            return;  // Stop processing this path due to a cycle
        }

        // Add the current node to the current path and mark it visited in the DFS path
        currentPath.add(currentNode.getName());
        pathVisited.add(currentNode);

        // Recur for all adjacent nodes (neighbors) of the current node
        for (Microservice neighbor : graph.getAdjacency(currentNode)) {
            dfs(neighbor, pathVisited);  // Recursive DFS call for all neighbors
        }

        // If the current path reaches or exceeds the chain length and no cycle was detected, add it to the list of service chains
        if (currentPath.size() >= CHAIN_LENGTH && !hasCycle) {
            allChains.add(new ArrayList<>(currentPath));  // Add a copy of the current path
        }

        // Backtrack: remove the current node from the path and the DFS path set
        currentPath.remove(currentPath.size() - 1);
        pathVisited.remove(currentNode);
    }
}



