package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.sdg.EndpointCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jgrapht.graph.DefaultGraphIterables;

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
    Absolute Dependency of the Service - number of services invoked by the service
     */
    private final Map<String, Integer> ADS;
    /**
    Absolute Criticality of the Service - product of AIS and ADS
     */
    private final Map<String, Integer> ACS;
    /**
    Average number of Directly Connected Services - average of ADS;
     */
    private final double ADCS;
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
    DegreeCoupling(ServiceDependencyGraph graph){
        AIS = new HashMap<>();
        ADS = new HashMap<>();
        ACS = new HashMap<>();
        int siy = 0;
        double tempADCS = 0.0;
        DefaultGraphIterables<String, EndpointCallEdge> GraphIter = new DefaultGraphIterables<>(graph);
        long N = GraphIter.vertexCount();
        Iterable<String> VertexIter = GraphIter.vertices();
        for (String vertex : VertexIter) {
            AIS.put(vertex, graph.incomingEdgesOf(vertex).stream().map(graph::getEdgeSource)
                    .collect(Collectors.toSet()).size());
            ADS.put(vertex, graph.outgoingEdgesOf(vertex).stream().map(graph::getEdgeTarget)
                    .collect(Collectors.toSet()).size());
            ACS.put(vertex, AIS.get(vertex)*ADS.get(vertex));
            tempADCS += ADS.get(vertex);
        }
        ADCS = tempADCS/N;
        Map<String, Set<String>> adjacency = graph.getAdjacency();
        double E = adjacency.values().stream().map(Set::size).mapToDouble(Integer::doubleValue).sum();
        SCF = E/(N*(N-1));
        for (String vertex: adjacency.keySet()) {
            for (String neighbour: adjacency.get(vertex)) {
                if (adjacency.get(neighbour).contains(vertex)) {
                    siy++;
                }
            }
        }
        SIY = siy/2; // each bidirectional edge was counted twice

    }
}
