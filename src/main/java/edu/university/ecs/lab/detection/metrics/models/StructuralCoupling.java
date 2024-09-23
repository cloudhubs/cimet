package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Getter;

import java.util.*;

/**
 * Class implementing the Structural Coupling Metric proposed (1)
 * (1) Panichella, S., Rahman, M. I., &#38; Taibi, D. (2021). Structural coupling for microservices. arXiv preprint arXiv:2103.04674
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
        for (Microservice vertexA: graph.vertexSet()) {
            for (Microservice vertexB: graph.vertexSet()) {
                List<String> pair = Arrays.asList(vertexA.getName(), vertexB.getName());
                if (!vertexA.equals(vertexB) && graph.containsEdge(vertexA, vertexB)) {
                    out_degree.put(pair,
                            graph.getAllEdges(vertexA, vertexB).stream().mapToDouble(graph::getEdgeWeight).sum());
                    in_degree.put(pair,
                            graph.getAllEdges(vertexB, vertexA).stream().mapToDouble(graph::getEdgeWeight).sum());
                    degree.put(pair, in_degree.get(pair)+out_degree.get(pair));
                    LWF.put(pair, (1+out_degree.get(pair))/(1+degree.get(pair)));
                }
            }
        }
        double max_degree;
        try {
            max_degree = degree.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                    orElseThrow(() -> new NullPointerException("Degree map is empty after processing the SDG"));
        } catch (NullPointerException e) {
            avgLWF = 0.0;
            avgGWF = 0.0;
            avgSC = 0.0;
            maxLWF = 0.0;
            maxGWF = 0.0;
            maxSC = 0.0;
            stdLWF = 0.0;
            stdGWF = 0.0;
            stdSC = 0.0;
            return;
        }
        for (List<String> pair: LWF.keySet()) {
            GWF.put(pair, degree.get(pair)/max_degree);
            SC.put(pair, 1-1/degree.get(pair)-LWF.get(pair)*GWF.get(pair));
        }

        // Amount of actually connected pairs
        long N = LWF.size();

        // Averages
        avgLWF = LWF.values().stream().mapToDouble(Double::doubleValue).sum() / N;
        avgGWF = GWF.values().stream().mapToDouble(Double::doubleValue).sum() / N;
        avgSC = SC.values().stream().mapToDouble(Double::doubleValue).sum() / N;

        // Maxima
        maxLWF = LWF.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                 orElse(0.0);
        maxGWF = GWF.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                 orElse(0.0);
        maxSC = SC.values().stream().max(Comparator.comparingDouble(Double::doubleValue)).
                orElse(0.0);

        // Standard deviations
        stdLWF = Math.sqrt(LWF.values().stream().map(value -> Math.pow(value - avgLWF, 2))
                 .mapToDouble(Double::doubleValue).sum() / N);
        stdGWF = Math.sqrt(GWF.values().stream().map(value -> Math.pow(value - avgGWF, 2))
                .mapToDouble(Double::doubleValue).sum() / N);
        stdSC = Math.sqrt(SC.values().stream().map(value -> Math.pow(value - avgSC, 2))
                 .mapToDouble(Double::doubleValue).sum() / N);
    }
}
