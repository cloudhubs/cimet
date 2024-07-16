package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;

import java.util.*;

/**
 * Service class for detecting and managing service chains in a network graph.
 */
public class ServiceChainService {

    /**
     * Length of the chain to consider an anti-pattern.
     */
    private final int CHAIN_LENGTH;
    
    /**
     * Constructs the service with a default chain length of 3.
     */
    public ServiceChainService() {
        this.CHAIN_LENGTH = 3;
    }
    
    /**
     * Constructs the service with a specified chain length.
     * 
     * @param CHAIN_LENGTH the length of the chain to consider an anti-pattern
     */
    public ServiceChainService(int CHAIN_LENGTH) {
        this.CHAIN_LENGTH = CHAIN_LENGTH;
    }

    /**
     * Retrieves all service chains from the given network graph.
     *
     * @param graph the network graph to analyze
     * @return a ServiceChain object representing all detected service chains
     */
    public ServiceChain getServiceChains(ServiceDependencyGraph graph) {
        List<List<String>> allChains = new ArrayList<>();
        Map<Microservice, Set<Microservice>> adjacencyList = graph.getAdjacency();

        Set<Microservice> globalVisited = new HashSet<>();

        for (Microservice node : graph.vertexSet()) {
            if (!globalVisited.contains(node)) {
                dfs(node, new ArrayList<>(), allChains, adjacencyList, globalVisited);
            }
        }

        return new ServiceChain(allChains);
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
    private void dfs(Microservice currentNode, List<String> currentPath, List<List<String>> allChains,
                     Map<Microservice, Set<Microservice>> adjacencyList, Set<Microservice> globalVisited) {
        if (globalVisited.contains(currentNode)) {
            return;
        }

        currentPath.add(currentNode.getName());
        globalVisited.add(currentNode);

        Set<Microservice> neighbors = adjacencyList.get(currentNode);
        if (neighbors != null && !neighbors.isEmpty()) {
            for (Microservice neighbor : neighbors) {
                if (!globalVisited.contains(neighbor)) {
                    dfs(neighbor, currentPath, allChains, adjacencyList, globalVisited);
                }
            }
        }

        if (currentPath.size() >= CHAIN_LENGTH) {
            allChains.add(new ArrayList<>(currentPath));
        }

        currentPath.remove(currentPath.size() - 1);
    }
}
