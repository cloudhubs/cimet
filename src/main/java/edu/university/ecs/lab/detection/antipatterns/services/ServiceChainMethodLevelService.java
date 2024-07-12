package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Method;
import edu.university.ecs.lab.common.models.sdg.MethodDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;

import java.util.*;

/**
 * Service class for detecting and managing service chains in a network graph.
 */
public class ServiceChainMethodLevelService {

    /**
     * Length of the chain to consider an anti-pattern.
     */
    private final int CHAIN_LENGTH;
    private Set<Method> visited = new HashSet<>();
    private List<List<String>> allChains = new ArrayList<>();
    private MethodDependencyGraph graph = null;
    private List<String> currentPath = new ArrayList<>();

    /**
     * Constructs the service with a default chain length of 3.
     */
    public ServiceChainMethodLevelService() {
        this.CHAIN_LENGTH = 3;
    }

    /**
     * Constructs the service with a specified chain length.
     *
     * @param CHAIN_LENGTH the length of the chain to consider an anti-pattern
     */
    public ServiceChainMethodLevelService(int CHAIN_LENGTH) {
        this.CHAIN_LENGTH = CHAIN_LENGTH;
    }

    /**
     * Retrieves all service chains from the given network graph.
     *
     * @param graph the network graph to analyze
     * @return a ServiceChain object representing all detected service chains
     */
    public ServiceChain getServiceChains(MethodDependencyGraph graph) {
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
    private void dfs(Method currentNode) {
        visited.add(currentNode);
        String currentNodeMS = currentNode.getMicroserviceName();
        boolean switched_service = currentPath.isEmpty() || !currentNodeMS.equals(currentPath.get(currentPath.size() - 1));
        if (switched_service) {
            currentPath.add(currentNodeMS);
        }

        graph.getAdjacency(currentNode).stream().filter(neighbor -> !visited.contains(neighbor)).forEach(this::dfs);

        if (switched_service) {
            if (currentPath.size() > CHAIN_LENGTH) {
                allChains.add(new ArrayList<>(currentPath));
            }

            currentPath.remove(currentPath.size() - 1);
        }
    }
}
