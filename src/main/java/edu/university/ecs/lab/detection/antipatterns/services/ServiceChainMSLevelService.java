package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;

import java.util.*;

/**
 * Service class for detecting and managing service chains in a network graph.
 */
public class ServiceChainMSLevelService {

    /**
     * Length of the chain to consider an anti-pattern.
     */
    protected static final int DEFAULT_CHAIN_LENGTH = 3;
    private final int CHAIN_LENGTH;
    private Set<Microservice> visited = new HashSet<>();
    private List<List<String>> allChains = new ArrayList<>();
    private ServiceDependencyGraph graph = null;
    private List<String> currentPath = new ArrayList<>();

    /**
     * Constructs the service with a default chain length of 3.
     */
    public ServiceChainMSLevelService() {
        this.CHAIN_LENGTH = DEFAULT_CHAIN_LENGTH;
    }
    
    /**
     * Constructs the service with a specified chain length.
     * 
     * @param CHAIN_LENGTH the length of the chain to consider an anti-pattern
     */
    public ServiceChainMSLevelService(int CHAIN_LENGTH) {
        this.CHAIN_LENGTH = CHAIN_LENGTH;
    }

    /**
     * Retrieves all service chains from the given network graph.
     *
     * @param graph the network graph to analyze
     * @return a ServiceChain object representing all detected service chains
     */
    public ServiceChain getServiceChains(ServiceDependencyGraph graph) {
        allChains = new ArrayList<>();
        this.graph = graph;
        visited = new HashSet<>();

        graph.vertexSet().stream().filter(node -> !visited.contains(node)).forEach(node -> {
            this.currentPath = new ArrayList<>();
            dfs(node);
        });

        return new ServiceChain(allChains);
    }

    /**
     * Depth-first search (DFS) to explore and detect service chains starting from currentNode.
     *
     * @param currentNode the current node being visited
     */
    private void dfs(Microservice currentNode) {
        currentPath.add(currentNode.getName());
        visited.add(currentNode);

        graph.getAdjacency(currentNode).stream().filter(neighbor -> !visited.contains(neighbor)).forEach(this::dfs);

        if (currentPath.size() >= CHAIN_LENGTH) {
            allChains.add(new ArrayList<>(currentPath));
        }

        currentPath.remove(currentPath.size() - 1);
    }
}
