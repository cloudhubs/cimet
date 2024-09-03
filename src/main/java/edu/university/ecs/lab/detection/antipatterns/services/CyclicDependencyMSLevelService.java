package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency;

import java.util.*;

/**
 * Service class for detecting cyclic dependencies in a microservice network graph.
 */
public class CyclicDependencyMSLevelService {

    private List<List<String>> allCycles = new ArrayList<>(); //
    private Set<Microservice> visited = new HashSet<>();
    private Set<Microservice> recStack = new HashSet<>();
    private Map<Microservice, Microservice> parentMap = new HashMap<>();
    private ServiceDependencyGraph graph = null;

    /**
     * Finds all cyclic dependencies in the given network graph.
     * 
     * @param graph the network graph to analyze
     * @return a CyclicDependency object representing all detected cycles
     */
    public CyclicDependency findCyclicDependencies(ServiceDependencyGraph graph) {
        allCycles = new ArrayList<>();
        visited = new HashSet<>();
        recStack = new HashSet<>();
        parentMap = new HashMap<>();
        this.graph = graph;

        // Find cycles for each node in service dependency graph
        graph.vertexSet().stream().filter(node -> !visited.contains(node)).forEach(this::findCycles);

        return new CyclicDependency(allCycles);
    }

    /**
     * Checks if there is a cycle starting from the current node.
     *
     * @param currentNode        the current node to check
     */
    private void findCycles(Microservice currentNode) {
        // Add current node to visited list and recStack
        visited.add(currentNode);
        recStack.add(currentNode);

        // Iterate through each node in adjacency list
        this.graph.getAdjacency(currentNode).forEach(neighbor -> {
            // If adjacent node has not already been checked, add current node and neigbor 
            // node to parent map and recursively find cycles for neighbor node. Otherwise, 
            // use parent map to reconstruct cycle path and add to allCycles list.
            if (!visited.contains(neighbor)) {
                parentMap.put(neighbor, currentNode);
                findCycles(neighbor);
            } else if (recStack.contains(neighbor)) {
                List<String> cyclePath = reconstructCyclePath(neighbor, currentNode);
                allCycles.add(cyclePath);
            }
        });
        recStack.remove(currentNode);
    }

    /**
     * Reconstructs the cycle path from startNode to currentNode using the parentMap.
     * 
     * @param startNode  the start node of the cycle
     * @param currentNode the current node to reconstruct path to
     * @return the list of nodes representing the cycle path
     */
    private List<String> reconstructCyclePath(Microservice startNode, Microservice currentNode) {
        List<String> fullCyclePath = new ArrayList<>();
        Microservice node = currentNode;

        // Iterate through each node in parent map until startNode is reached, adding each to 
        // the cycle path
        fullCyclePath.add(startNode.getName());
        while (node != null && !node.equals(startNode)) {
            fullCyclePath.add(node.getName());
            node = parentMap.get(node);
        }
        fullCyclePath.add(startNode.getName());

        return fullCyclePath;
    }
}
