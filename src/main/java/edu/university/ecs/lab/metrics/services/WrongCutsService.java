package edu.university.ecs.lab.metrics.services;

import edu.university.ecs.lab.common.models.Edge;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.metrics.models.metrics.WrongCuts;

import java.util.*;

public class WrongCutsService {
    public List<WrongCuts> identifyAndReportWrongCuts(NetworkGraph graph) {
        List<Set<String>> wrongCutsList = detectWrongCuts(graph);
        List<WrongCuts> wrongCutsObjects = new ArrayList<>();

        for (Set<String> wrongCut : wrongCutsList) {
            wrongCutsObjects.add(new WrongCuts(wrongCut));
        }

        return wrongCutsObjects;
    }

    public List<Set<String>> detectWrongCuts(NetworkGraph graph) {
        Map<String, List<String>> adjacencyList = buildAdjacencyList(graph);
        Set<String> visited = new HashSet<>();
        List<Set<String>> wrongCuts = new ArrayList<>();

        for (String node : graph.getNodes()) {
            if (!visited.contains(node)) {
                Set<String> cluster = new HashSet<>();
                dfs(node, adjacencyList, visited, cluster);
                wrongCuts.add(cluster);
            }
        }

        return wrongCuts;
    }

    private Map<String, List<String>> buildAdjacencyList(NetworkGraph graph) {
        Map<String, List<String>> adjacencyList = new HashMap<>();

        for (String node : graph.getNodes()) {
            adjacencyList.put(node, new ArrayList<>());
        }

        for (Edge edge : graph.getEdges()) {
            adjacencyList.get(edge.getSource()).add(edge.getTarget());
        }

        return adjacencyList;
    }

    private void dfs(String currentNode, Map<String, List<String>> adjacencyList, Set<String> visited, Set<String> cluster) {
        visited.add(currentNode);
        cluster.add(currentNode);

        for (String neighbor : adjacencyList.getOrDefault(currentNode, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacencyList, visited, cluster);
            }
        }
    }
}
