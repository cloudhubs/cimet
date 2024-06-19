package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
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
     * @return a list of cyclic dependencies found
     */
    public List<CyclicDependency> findCyclicDependencies(NetworkGraph graph) {
        List<CyclicDependency> cyclicDependencies = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();

        for (String node : graph.getNodes()) {
            if (!visited.contains(node)) {
                if (hasCycle(node, visited, recStack, graph, parentMap, cyclicDependencies)) {
                    // Continue searching for other cycles
                }
            }
        }

        return cyclicDependencies;
    }

    /**
     * Checks if there is a cycle starting from the current node.
     * 
     * @param currentNode      the current node to check
     * @param visited          set of visited nodes
     * @param recStack         stack of nodes in the current recursion stack
     * @param graph            the network graph
     * @param parentMap        map of node to its parent in the traversal
     * @param cyclicDependencies list to store detected cyclic dependencies
     * @return true if a cycle is found, false otherwise
     */
    private boolean hasCycle(String currentNode, Set<String> visited, Set<String> recStack, NetworkGraph graph, Map<String, String> parentMap, List<CyclicDependency> cyclicDependencies) {
        visited.add(currentNode);
        recStack.add(currentNode);

        List<String> neighbors = getNeighbors(currentNode, graph);
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                parentMap.put(neighbor, currentNode);
                if (hasCycle(neighbor, visited, recStack, graph, parentMap, cyclicDependencies)) {
                    // Continue searching for other cycles
                }
            } else if (recStack.contains(neighbor)) {
                List<String> cyclePath = reconstructCyclePath(neighbor, currentNode, parentMap);
                cyclicDependencies.add(new CyclicDependency(new ArrayList<>(cyclePath)));
            }
        }

        recStack.remove(currentNode);

        return false;
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

    /**
     * Retrieves the neighbors of the currentNode from the graph.
     * 
     * @param currentNode the node whose neighbors are to be retrieved
     * @param graph       the network graph
     * @return the list of neighbors of the currentNode
     */
    private List<String> getNeighbors(String currentNode, NetworkGraph graph) {
        List<String> neighbors = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().equals(currentNode)) {
                neighbors.add(edge.getTarget());
            }
        }
        return neighbors;
    }
}
