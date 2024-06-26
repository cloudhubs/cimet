package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.sdg.EndpointCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Getter;
import org.jgrapht.alg.clustering.UndirectedModularityMeasurer;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class implementing the modularity metric of partitioning a graph into strongly connected components
 */
@Getter
public class ConnectedComponentsModularity {

    /**
     * Strongly connected components of the graph.
     */
    private final List<Set<String>> SCC;
    /**
     * Modularity of clusters of SCC
     */
    private final double modularity;

    /**
     * Construct the Strongly Connected Components of the graph and calculate the modularity of such partition
     * @param graph Service Dependency Graph to analyze
     */
    public ConnectedComponentsModularity(ServiceDependencyGraph graph) {
        KosarajuStrongConnectivityInspector<String, EndpointCallEdge> inspector = new KosarajuStrongConnectivityInspector<>(graph);
        SCC = new ArrayList<>();
        TarjanSCC(graph);
        AsUndirectedGraph<String, EndpointCallEdge> undirected = new AsUndirectedGraph<>(graph);
        UndirectedModularityMeasurer<String, EndpointCallEdge> measurer = new UndirectedModularityMeasurer<>(undirected);
        modularity = measurer.modularity(SCC);
    }
    private void TarjanSCC(ServiceDependencyGraph graph) {
        HashMap<String, Integer> Vindex = new HashMap<>();
        HashMap<String, Integer> LowLink = new HashMap<>();
        HashMap<String, Boolean> onStack = new HashMap<>();
        Map<String, Set<String>> adjacency = graph.getAdjacency();
        Integer index = 0;
        ArrayList<String> S = new ArrayList<>();
        for (String vertex: graph.vertexSet()) {
            if (!Vindex.containsKey(vertex)) {
                index = StrongConnect(vertex, Vindex, LowLink, index, S, onStack, adjacency);
            }
        }
    }

    private Integer StrongConnect(String v, Map<String, Integer> Vindex, Map<String, Integer> LowLink, Integer index,
        List<String> S, Map<String, Boolean> onStack, Map<String, Set<String>> adjacency) {
        Vindex.put(v, index);
        LowLink.put(v, index);
        index++;
        S.add(v);
        onStack.put(v, Boolean.TRUE);
        for (String w: adjacency.get(v)) {
            if (!Vindex.containsKey(w)) {
                index = StrongConnect(w, Vindex, LowLink, index, S, onStack, adjacency);
                LowLink.put(v, Math.min(LowLink.get(v), LowLink.get(w)));
            }
            else if (onStack.get(w)) {
                LowLink.put(v, Math.min(LowLink.get(v), Vindex.get(w)));
            }
        }
        if (LowLink.get(v).equals(Vindex.get(v))) {
           HashSet<String> scc = new HashSet<>();
           String w;
           do {
               w = S.remove(S.size()-1);
               onStack.put(w, Boolean.FALSE);
               scc.add(w);
           } while (!w.equals(v));
           SCC.add(scc);
        }
        return index;

    }
}
