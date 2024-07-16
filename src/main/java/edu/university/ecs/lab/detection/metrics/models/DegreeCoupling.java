package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Class implementing the calculation of degree-related Coupling metrics according to [1]
 * [1] Bogner, J., Wagner, S., & Zimmermann, A. (2017, October).
 * Automatically measuring the maintainability of service-and microservice-based systems: a literature review.
 * In Proceedings of the 27th international workshop on software measurement and 12th international conference
 * on software process and product measurement (pp. 107-115).
 */
@Getter
public class DegreeCoupling {
    /**
    Absolute Importance of the Service - numbers of services invoking given service
     */
    private final Map<String, Integer> AIS;
    /**
     * Average of AIS
     */
    private final double avgAIS;
    /**
     * Maximum of AIS
     */
    private final int maxAIS;
    /**
     * Standard deviation of AIS
     */
    private final double stdAIS;
    /**
    Absolute Dependency of the Service - number of services invoked by the service
     */
    private final Map<String, Integer> ADS;
    /**
     Average number of Directly Connected Services - average of ADS;
     */
    private final double ADCS;
    /**
     * Maximum of ADS
     */
    private final int maxADS;
    /**
     * Standard deviation of ADS
     */
    private final double stdADS;
    /**
    Absolute Criticality of the Service - product of AIS and ADS
     */
    private final Map<String, Integer> ACS;
    /**
     * Average of ACS
     */
    private final double avgACS;
    /**
     * Maximum of ACS
     */
    private final int maxACS;
    /**
     * Standard deviation of ACS
     */
    private final double stdACS;
    /**
    Service coupling factor (graph density)
     */
    private final double SCF;
    /**
     * Service Interdependence in the System - amount of service pairs that bidirectionally call each other
     */
    private final int SIY;


    /**
     * Calculate the degree-related Coupling metrics for a given Service Dependency Graph
     * @param graph - Service Dependency Graph to study
     */
    public DegreeCoupling(ServiceDependencyGraph graph){
        // AIS, ADS, ACS
        AIS = new HashMap<>();
        ADS = new HashMap<>();
        ACS = new HashMap<>();
        for (Microservice vertex : graph.vertexSet()) {
            String name = vertex.getName();
            AIS.put(name, graph.incomingEdgesOf(vertex).stream().map(graph::getEdgeSource)
                    .collect(Collectors.toSet()).size());
            ADS.put(name, graph.outgoingEdgesOf(vertex).stream().map(graph::getEdgeTarget)
                    .collect(Collectors.toSet()).size());
            ACS.put(name, AIS.get(name)*ADS.get(name));
        }

        //  Service coupling factor (graph density)
        Map<Microservice, Set<Microservice>> adjacency = graph.getAdjacency();
        double E = adjacency.values().stream().map(Set::size).mapToDouble(Integer::doubleValue).sum();
        long N = AIS.size();
        SCF = E/(N*(N-1));

        // Service Interdependence in the System
        int siy = 0;
        for (Microservice vertex: adjacency.keySet()) {
            for (Microservice neighbour: adjacency.get(vertex)) {
                if (adjacency.get(neighbour).contains(vertex)) {
                    siy++;
                }
            }
        }
        SIY = siy/2; // each bidirectional edge was counted twice

        // Averages
        avgAIS = AIS.values().stream().mapToDouble(Integer::doubleValue).sum() / N;
        ADCS = ADS.values().stream().mapToDouble(Integer::doubleValue).sum() / N;
        avgACS = ACS.values().stream().mapToDouble(Integer::doubleValue).sum() / N;

        // Maxima
        maxAIS = AIS.values().stream().max(Comparator.comparingInt(Integer::intValue)).
            orElseThrow(() -> new RuntimeException("Cannot find maximum of AIS"));
        maxADS = ADS.values().stream().max(Comparator.comparingInt(Integer::intValue)).
                orElseThrow(() -> new RuntimeException("Cannot find maximum of ADS"));
        maxACS = ACS.values().stream().max(Comparator.comparingInt(Integer::intValue)).
                orElseThrow(() -> new RuntimeException("Cannot find maximum of ACS"));

        // Standard deviations
        stdAIS = Math.sqrt(AIS.values().stream().map(value -> Math.pow(value - avgAIS, 2))
                .mapToDouble(Double::doubleValue).sum() / N);
        stdADS = Math.sqrt(ADS.values().stream().map(value -> Math.pow(value - ADCS, 2))
                .mapToDouble(Double::doubleValue).sum() / N);
        stdACS = Math.sqrt(ACS.values().stream().map(value -> Math.pow(value - avgACS, 2))
                .mapToDouble(Double::doubleValue).sum() / N);

    }
}
