package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Getter;

import java.util.*;

/**
 * Class implementing the Structural Coupling Metric proposed in [1]
 * [1] Panichella, S., Rahman, M. I., & Taibi, D. (2021). Structural coupling for microservices. arXiv preprint arXiv:2103.04674
 */
@Getter
public class StructuralCoupling {

    /**
     * Local Weight Factor(s1, s2) = (1+out_degree(s1, s2))/(1+degree(s1, s2))
     */
    private final Map<List<String>, Double> LWF;
    /**
     * Maximum of LWF
     */
    private final double maxLWF;
    /**
     * Average of LWF
     */
    private final double avgLWF;
    /**
     * Standard deviation of LWF
     */
    private final double stdLWF;
    /**
     * Global Weight Factor(s1, s2) = degree(s1, s2)/max_degree
     */
    private final Map<List<String>, Double> GWF;
    /**
     * Maximum of GWF
     */
    private final double maxGWF;
    /**
     * Average of GWF
     */
    private final double avgGWF;
    /**
     * Standard deviation of GWF
     */
    private final double stdGWF;
    /**
     * Structural Coupling(s1, s2) = 1 - 1/degree(s1, s2) - LWF(s1, s2)*GWF(s1, s2)
     */
    private final Map<List<String>, Double> SC;
    /**
     * Maximum of SC
     */
    private final double maxSC;
    /**
     * Average of SC
     */
    private final double avgSC;
    /**
     * Standard deviation of SC
     */
    private final double stdSC;

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

        // Amount of actually connected pairs
        long N = LWF.keySet().stream().filter(pair -> graph.containsEdge(pair.get(0), pair.get(1))).count();

        // Averages
        avgLWF = LWF.values().stream().mapToDouble(Double::doubleValue).sum() / N;
        avgGWF = GWF.values().stream().mapToDouble(Double::doubleValue).sum() / N;
        avgSC = SC.values().stream().mapToDouble(Double::doubleValue).sum() / N;

        // Maxima
        maxLWF = LWF.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                 orElseThrow(() -> new RuntimeException("Cannot find maximum of LWF"));
        maxGWF = GWF.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                 orElseThrow(() -> new RuntimeException("Cannot find maximum of GWF"));
        maxSC = SC.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                orElseThrow(() -> new RuntimeException("Cannot find maximum of SC"));

        // Standard deviations
        stdLWF = Math.sqrt(LWF.values().stream().map(value -> Math.pow(value - avgLWF, 2))
                 .mapToDouble(Double::doubleValue).sum() / N);
        stdGWF = Math.sqrt(GWF.values().stream().map(value -> Math.pow(value - avgGWF, 2))
                .mapToDouble(Double::doubleValue).sum() / N);
        stdSC = Math.sqrt(SC.values().stream().map(value -> Math.pow(value - avgSC, 2))
                 .mapToDouble(Double::doubleValue).sum() / N);
    }
}
