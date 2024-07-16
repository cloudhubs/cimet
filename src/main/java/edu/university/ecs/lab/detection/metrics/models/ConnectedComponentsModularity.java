package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.RestCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Getter;
import org.jgrapht.alg.clustering.UndirectedModularityMeasurer;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class implementing the modularity metric of partitioning a graph into strongly connected components
 */
@Getter
public class ConnectedComponentsModularity {

    /**
     * Strongly connected components of the graph.
     */
    private final List<Set<Microservice>> SCC;
    /**
     * Modularity of clusters of SCC
     */
    private final double modularity;

    /**
     * Construct the Strongly Connected Components of the graph and calculate the modularity of such partition
     * @param graph Service Dependency Graph to analyze
     */
    public ConnectedComponentsModularity(ServiceDependencyGraph graph) {
        KosarajuStrongConnectivityInspector<Microservice, RestCallEdge> inspector = new KosarajuStrongConnectivityInspector<>(graph);
        SCC = inspector.stronglyConnectedSets();
        // all nodes that are part of some SCC
        Set<Microservice> allSCC = SCC.stream().flatMap(Set::stream).collect(Collectors.toSet());
        // all nodes that are now part of any SCC
        Set<Microservice> notSCC = graph.vertexSet().stream().filter(vertex -> !allSCC.contains(vertex)).collect(Collectors.toSet());
        // Partition of all nodes (SCCs + extra nodes)
        ArrayList<Set<Microservice>> allNodes = new ArrayList<>(SCC);
        allNodes.add(notSCC);
        AsUndirectedGraph<Microservice, RestCallEdge> undirected = new AsUndirectedGraph<>(graph);
        UndirectedModularityMeasurer<Microservice, RestCallEdge> measurer = new UndirectedModularityMeasurer<>(undirected);
        modularity = measurer.modularity(allNodes);
    }
}
