package edu.university.ecs.lab.detection.antipatterns.services;

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
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();
        Map<String, Set<String>> adjacency = graph.getAdjacency();

        for (String node : graph.vertexSet()) {
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
    private void findCycles(String currentNode, Map<String, Set<String>> adjacencyList, Set<String> visited, Set<String> recStack, Map<String, String> parentMap, List<List<String>> allCycles) {
        visited.add(currentNode);
        recStack.add(currentNode);

        for (String neighbor : adjacencyList.get(currentNode)) {
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
    private List<String> reconstructCyclePath(String startNode, String currentNode, Map<String, String> parentMap) {
        List<String> fullCyclePath = new ArrayList<>();
        String node = currentNode;

        fullCyclePath.add(startNode);
        while (node != null && !node.equals(startNode)) {
            fullCyclePath.add(node);
            node = parentMap.get(node);
        }
        fullCyclePath.add(startNode);

        return fullCyclePath;
    }
}
