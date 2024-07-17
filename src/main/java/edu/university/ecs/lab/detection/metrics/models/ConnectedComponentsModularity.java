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
        AsUndirectedGraph<Microservice, RestCallEdge> undirected = new AsUndirectedGraph<>(graph);
        UndirectedModularityMeasurer<Microservice, RestCallEdge> measurer = new UndirectedModularityMeasurer<>(undirected);
        if (SCC.size() == graph.vertexSet().size()) {
            modularity = 0.0;
            return;
        }
        modularity = measurer.modularity(SCC);
    }
}
