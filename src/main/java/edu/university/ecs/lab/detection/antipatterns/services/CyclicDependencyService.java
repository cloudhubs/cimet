package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency;

import java.util.*;

public class CyclicDependencyService {

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
                // Found a cycle, reconstruct the cycle path
                List<String> cyclePath = reconstructCyclePath(neighbor, currentNode, parentMap);
                cyclicDependencies.add(new CyclicDependency(new ArrayList<>(cyclePath)));
            }
        }

        // Backtrack
        recStack.remove(currentNode);

        return false;
    }

    private List<String> reconstructCyclePath(String startNode, String currentNode, Map<String, String> parentMap) {
        List<String> fullCyclePath = new ArrayList<>();
        String node = currentNode;

        fullCyclePath.add(startNode);
        while (node != null && !node.equals(startNode)) {
            fullCyclePath.add(node);
            node = parentMap.get(node);
        }
        fullCyclePath.add(startNode);  // To complete the cycle

        return fullCyclePath;
    }

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
