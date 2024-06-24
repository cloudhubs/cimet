package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Getter;

import java.util.*;

@Getter
/**
 * Class implementing the Structural Coupling Metric proposed in [1]
 * [1] Panichella, S., Rahman, M. I., & Taibi, D. (2021). Structural coupling for microservices. arXiv preprint arXiv:2103.04674
 */
public class StructuralCoupling {

    /**
     * Local Weight Factor(s1, s2) = (1+out_degree(s1, s2))/(1+degree(s1, s2))
     */
    private final Map<List<String>, Double> LWF;
    /**
     * Global Weight Factor(s1, s2) = degree(s1, s2)/max_degree
     */
    private final Map<List<String>, Double> GWF;
    /**
     * Structural Coupling(s1, s2) = 1 - 1/degree(s1, s2) - LWF(s1, s2)*GWF(s1, s2)
     */
    private final Map<List<String>, Double> SC;

    /**
     * Calculate the Structural Coupling and related metrics for a given Service Dependency Graph
     * @param graph - Service Dependency Graph to study
     */
    public StructuralCoupling(ServiceDependencyGraph graph) {
        LWF = new HashMap<>();
        GWF = new HashMap<>();
        SC = new HashMap<>();
        Map<List<String>, Double> in_degree = new HashMap<>();
        Map<List<String>, Double> out_degree = new HashMap<>();
        Map<List<String>, Double> degree = new HashMap<>();
        for (String vertexA: graph.vertexSet()) {
            for (String vertexB: graph.vertexSet()) {
                List<String> pair = Arrays.asList(vertexA, vertexB);
                if (!vertexA.equals(vertexB)) {
                    out_degree.put(pair,
                            graph.getAllEdges(vertexA, vertexB).stream().mapToDouble(graph::getEdgeWeight).sum());
                    in_degree.put(pair,
                            graph.getAllEdges(vertexB, vertexA).stream().mapToDouble(graph::getEdgeWeight).sum());
                    degree.put(pair, in_degree.get(pair)+out_degree.get(pair));
                    LWF.put(pair, (1+out_degree.get(pair))/(1+degree.get(pair)));
                }
            }
        }
        double max_degree = degree.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                orElseThrow(() -> new RuntimeException("Degree map is empty after processing the SDG"));
       for (String vertexA: graph.vertexSet()) {
           for (String vertexB: graph.vertexSet()) {
               List<String> pair = Arrays.asList(vertexA, vertexB);
               if (!vertexA.equals(vertexB)) {
                   GWF.put(pair, degree.get(pair)/max_degree);
                   SC.put(pair, 1-1/degree.get(pair)-LWF.get(pair)*GWF.get(pair));
               }
           }
       }
    }
}
