package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Data;
import java.util.*;

/**
 * Architectural Rule 21 Class: Wrongcuts Service
 */
@Data
public class AR21 extends AbstractAR {

    /**
     * Architectural rule 21 details
     */
    protected static final String TYPE = "Architectural Rule 21";
    protected static final String NAME = "Wrongcuts Service";
    protected static final String DESC = "";
    
    private String oldCommitID;
    private String newCommitID;
    protected JsonObject metaData;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
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

    /**
     * Scan and compare old microservice system and new microservice system to identify wrongly interconnected services
     * 
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list of wrongly interconnected service clusters
     */
    public static List<AR21> scan(MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<AR21> archRules = new ArrayList<>();

        ServiceDependencyGraph graph = new ServiceDependencyGraph(newSystem);
        List<Set<String>> wrongCuts = detectWrongCuts(graph);

        wrongCuts.forEach(s -> {
            AR21 archRule21 = new AR21();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Microservice", String.join(",", s));
            archRule21.setMetaData(jsonObject);
            archRule21.setOldCommitID(oldSystem.getCommitID());
            archRule21.setNewCommitID(newSystem.getCommitID());
            archRules.add(archRule21);
        });

        return archRules;

    }

    /**
     * Identifies and reports clusters of wrongly interconnected services based on the provided network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of {@link WrongCuts} objects, each representing a cluster of wrongly interconnected services.
     */
//    public List<WrongCuts> identifyAndReportWrongCuts(ServiceDependencyGraph graph) {
//        List<Set<String>> wrongCutsList = detectWrongCuts(graph);
//
//        return wrongCutsList.stream().filter(wrongCut -> wrongCut.size() > 1).map(WrongCuts::new)
//                .collect(Collectors.toList());
//    }

    /**
     * Detects all clusters of wrongly interconnected services in the given network graph.
     *
     * @param graph The network graph representing microservices and their dependencies.
     * @return A list of sets, each containing services that are wrongly interconnected (forming a cluster).
     */
    public static List<Set<String>> detectWrongCuts(ServiceDependencyGraph graph) {
        Map<Microservice, Set<Microservice>> adjacencyList = graph.getAdjacency();
        Set<Microservice> visited = new HashSet<>();
        List<Set<String>> wrongCuts = new ArrayList<>();

        for (Microservice node : graph.vertexSet()) {
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
    private static void dfs(Microservice currentNode, Map<Microservice, Set<Microservice>> adjacencyList,
                            Set<Microservice> visited, Set<String> cluster) {
        visited.add(currentNode);
        cluster.add(currentNode.getName());

        for (Microservice neighbor : adjacencyList.get(currentNode)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacencyList, visited, cluster);
            }
        }
    }
}
