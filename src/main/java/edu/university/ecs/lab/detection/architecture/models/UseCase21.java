package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.detection.antipatterns.models.WrongCuts;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class UseCase21 extends AbstractUseCase {
    protected static final String TYPE = "UseCase21";
    protected static final String NAME = "Wrongcuts Service";
    protected static final Scope SCOPE = Scope.ENDPOINT;
    protected static final String DESC = "";
    private String oldCommitID;
    private String newCommitID;
    protected JsonObject metaData;

    @Override
    public List<? extends AbstractUseCase> checkUseCase() {
        // This method should return the list of UseCase3 instances relevant to UseCase7 logic, if any.
        ArrayList<UseCase21> useCases = new ArrayList<>();
        return useCases;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public Scope getScope() {
        return SCOPE;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    public static List<UseCase21> scan(MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<UseCase21> useCases = new ArrayList<>();

        ServiceDependencyGraph graph = new ServiceDependencyGraph(newSystem);
        List<Set<String>> wrongCuts = detectWrongCuts(graph);

        wrongCuts.forEach(s -> {
            UseCase21 useCase = new UseCase21();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Microservice", String.join(",", s));
            useCase.setMetaData(jsonObject);
            useCase.setOldCommitID(oldSystem.getCommitID());
            useCase.setNewCommitID(newSystem.getCommitID());
            useCases.add(useCase);
        });

        return useCases;

    }

    /**
     * Identifies and reports clusters of wrongly interconnected services based on the provided network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of {@link WrongCuts} objects, each representing a cluster of wrongly interconnected services.
     */
    public List<WrongCuts> identifyAndReportWrongCuts(ServiceDependencyGraph graph) {
        List<Set<String>> wrongCutsList = detectWrongCuts(graph);

        return wrongCutsList.stream().filter(wrongCut -> wrongCut.size() > 1).map(WrongCuts::new)
                .collect(Collectors.toList());
    }

    /**
     * Detects all clusters of wrongly interconnected services in the given network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of sets, each containing services that are wrongly interconnected (forming a cluster).
     */
    public static List<Set<String>> detectWrongCuts(ServiceDependencyGraph graph) {
        Map<String, Set<String>> adjacencyList = graph.getAdjacency();
        Set<String> visited = new HashSet<>();
        List<Set<String>> wrongCuts = new ArrayList<>();

        for (String node : graph.vertexSet()) {
            if (!visited.contains(node)) {
                Set<String> cluster = new HashSet<>();
                dfs(node, adjacencyList, visited, cluster);
                wrongCuts.add(cluster);
            }
        }

        return wrongCuts;
    }

    /**
     * Performs Depth-First Search (DFS) to traverse and collect all nodes in the current cluster of wrong cuts.
     *
     * @param currentNode   The current node being visited.
     * @param adjacencyList The adjacency list representing the network graph.
     * @param visited       Set of visited nodes to avoid revisiting.
     * @param cluster       Set to collect all nodes belonging to the current cluster of wrong cuts.
     */
    private static void dfs(String currentNode, Map<String, Set<String>> adjacencyList, Set<String> visited, Set<String> cluster) {
        visited.add(currentNode);
        cluster.add(currentNode);

        for (String neighbor : adjacencyList.get(currentNode)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacencyList, visited, cluster);
            }
        }
    }
}
