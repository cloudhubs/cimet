package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.sdg.EndpointCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jgrapht.graph.DefaultGraphIterables;

public class DegreeMetrics {
    /*
    Absolute Importance of the Service - numbers of services invoking given service
     */
    @Getter private final Map<String, Integer> AIS;
    /*
    Absolute Dependency of the Service - number of services invoked by the service
     */
    @Getter private final Map<String, Integer> ADS;
    /*
    Absolute Criticality of the Service - product of AIS and ADS
     */
    @Getter private final Map<String, Integer> ACS;
    /*
    Average number of Directly Connected Services - average of ADS;
     */
    @Getter private final double ADCS;
    /*
    Service coupling factor (graph density)
     */
    @Getter private final double SCF;


    DegreeMetrics(ServiceDependencyGraph graph){
        AIS = new HashMap<>();
        ADS = new HashMap<>();
        ACS = new HashMap<>();
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
        double E = graph.getAdjacency().values().stream().map(Set::size).mapToDouble(Integer::doubleValue).sum();
        SCF = E/(N*(N-1));

    }
}
