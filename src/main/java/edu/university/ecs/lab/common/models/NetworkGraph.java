package edu.university.ecs.lab.common.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetworkGraph implements JsonSerializable {
    private static List<String> SERVICES = Arrays.asList("ts-admin-service",
            "ts-assurance-service",
            "ts-auth-service",
            "ts-cancel-service",
            "ts-config-service",
            "ts-consign-service",
            "ts-contacts-service",
            "ts-delivery-service",
            "ts-food-service",
            "ts-gateway",
            "ts-new-gateway",
            "ts-notification-service",
            "ts-order-related-service",
            "ts-order-service",
            "ts-preserve-service",
            "ts-price-service",
            "ts-rebook-service",
            "ts-route-service",
            "ts-security-service",
            "ts-station-service",
            "ts-travel-service",
            "ts-ui-dashboard",
            "ts-user-service");

    private String label;
    private String timestamp;
    private boolean directed;
    private boolean multigraph;
    private Set<String> nodes;
    private Set<Edge> edges;


    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        Gson gson1 = new Gson();
        Gson gson2 = new Gson();

        String nodesArray = gson1.toJson(nodes);
        String edgeArray = gson2.toJson(edges);

        jsonObject.addProperty("label", label);
        jsonObject.addProperty("timestamp", timestamp);
        jsonObject.addProperty("directed", directed);
        jsonObject.addProperty("multigraph", multigraph);
        jsonObject.addProperty("nodes", nodesArray);
        jsonObject.addProperty("edges", edgeArray);

        return jsonObject;
    }

    public void createGraph(MicroserviceSystem microserviceSystem) {
        this.label = "Test";
        this.timestamp = microserviceSystem.getCommitID();
        this.directed = true;
        this.multigraph = false;

        List<RestCall> restCalls = new ArrayList<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            for (JClass service : microservice.getServices()) {
                restCalls.addAll(service.getMethodCalls().stream()
                        .filter(methodCall -> methodCall instanceof RestCall)
                        .map(methodCall -> (RestCall) methodCall)
                        .collect(Collectors.toList()));
            }
        }

        List<Endpoint> endpoints = new ArrayList<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            for (JClass service : microservice.getControllers()) {
                endpoints.addAll(service.getMethods().stream()
                        .filter(methods -> methods instanceof Endpoint)
                        .map(methods -> (Endpoint) methods)
                        .collect(Collectors.toList()));
            }
        }

        List<Edge> edgesList = new ArrayList<>();
        this.nodes = new HashSet<>();

        for (RestCall restCall : restCalls) {
            for (Endpoint endpoint : endpoints) {
                if (restCall.getUrl().equals(endpoint.getUrl()) && restCall.getHttpMethod().equals(endpoint.getHttpMethod())) {
                    edgesList.add(new Edge(restCall.getMicroserviceName(), endpoint.getMicroserviceName(), endpoint.getUrl(), 0));
                    this.nodes.add(endpoint.getMicroserviceName());
                    this.nodes.add(restCall.getMicroserviceName());
                }
            }
        }

        this.edges = new HashSet<>();

        Map<Edge, Long> edgeDuplicateMap = edgesList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        this.edges = edgeDuplicateMap.entrySet().stream().map(entry -> {
            Edge edge = entry.getKey();
            edge.setWeight(Math.toIntExact(entry.getValue()));
            return edge;
        }).collect(Collectors.toSet());


    }
}
