package edu.university.ecs.lab.detection.metrics.models;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import lombok.Getter;

import java.util.*;

public class StructuralCoupling {

    @Getter private final Map<Set<String>, Double> LWF;
    @Getter private final Map<Set<String>, Double> GWF;
    @Getter private final Map<Set<String>, Double> SC;
    public StructuralCoupling(ServiceDependencyGraph graph) {
        LWF = new HashMap<>();
        GWF = new HashMap<>();
        SC = new HashMap<>();
        Map<Set<String>, Double> in_degree = new HashMap<>();
        Map<Set<String>, Double> out_degree = new HashMap<>();
        Map<Set<String>, Double> degree = new HashMap<>();
        for (String vertexA: graph.vertexSet()) {
            for (String vertexB: graph.vertexSet()) {
                HashSet<String> pair = new HashSet<>(Arrays.asList(vertexA, vertexB));
                if (!vertexA.equals(vertexB) && !LWF.containsKey(pair)) {
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
               HashSet<String> pair = new HashSet<>(Arrays.asList(vertexA, vertexB));
               if (!vertexA.equals(vertexB) && !GWF.containsKey(pair)) {
                   GWF.put(pair, degree.get(pair)/max_degree);
                   SC.put(pair, 1-1/degree.get(pair)-LWF.get(pair)*GWF.get(pair));
               }
           }
       }
    }
}
