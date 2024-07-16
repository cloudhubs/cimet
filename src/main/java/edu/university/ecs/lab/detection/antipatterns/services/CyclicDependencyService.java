package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency;

import java.util.*;

/**
 * Service class for detecting cyclic dependencies in a network graph.
 */
public class CyclicDependencyService {

    /**
     * Finds all cyclic dependencies in the given network graph.
     * 
     * @param graph the network graph to analyze
     * @return a CyclicDependency object representing all detected cycles
     */
    public CyclicDependency findCyclicDependencies(ServiceDependencyGraph graph) {
        List<List<String>> allCycles = new ArrayList<>();
        Set<Microservice> visited = new HashSet<>();
        Set<Microservice> recStack = new HashSet<>();
        Map<Microservice, Microservice> parentMap = new HashMap<>();
        Map<Microservice, Set<Microservice>> adjacency = graph.getAdjacency();

        for (Microservice node : graph.vertexSet()) {
            if (!visited.contains(node)) {
                findCycles(node, adjacency, visited, recStack, parentMap, allCycles);
            }
        }

        return new CyclicDependency(allCycles);
    }

    /**
     * Checks if there is a cycle starting from the current node.
     *
     * @param currentNode        the current node to check
     * @param adjacencyList      mapping from nodes to their outgoing neighbors
     * @param visited            set of visited nodes
     * @param recStack           stack of nodes in the current recursion stack
     * @param parentMap          map of node to its parent in the traversal
     * @param allCycles          list to store detected cyclic dependencies
     */
    private void findCycles(Microservice currentNode, Map<Microservice, Set<Microservice>> adjacencyList,
                            Set<Microservice> visited, Set<Microservice> recStack,
                            Map<Microservice,Microservice> parentMap, List<List<String>> allCycles) {
        visited.add(currentNode);
        recStack.add(currentNode);

        for (Microservice neighbor : adjacencyList.get(currentNode)) {
            if (!visited.contains(neighbor)) {
                parentMap.put(neighbor, currentNode);
                findCycles(neighbor, adjacencyList, visited, recStack, parentMap, allCycles);
            } else if (recStack.contains(neighbor)) {
                List<String> cyclePath = reconstructCyclePath(neighbor, currentNode, parentMap);
                allCycles.add(cyclePath);
            }
        }
        recStack.remove(currentNode);
    }

    /**
     * Reconstructs the cycle path from startNode to currentNode using the parentMap.
     * 
     * @param startNode  the start node of the cycle
     * @param currentNode the current node to reconstruct path to
     * @param parentMap  map of node to its parent in the traversal
     * @return the list of nodes representing the cycle path
     */
    private List<String> reconstructCyclePath(Microservice startNode, Microservice currentNode,
                                              Map<Microservice, Microservice> parentMap) {
        List<String> fullCyclePath = new ArrayList<>();
        Microservice node = currentNode;

        fullCyclePath.add(startNode.getName());
        while (node != null && !node.equals(startNode)) {
            fullCyclePath.add(node.getName());
            node = parentMap.get(node);
        }
        fullCyclePath.add(startNode.getName());

        return fullCyclePath;
    }
}
