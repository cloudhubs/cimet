package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class NetworkGraph implements JsonSerializable {
//    private static final List<String> SERVICES = Arrays.asList("ts-admin-service",
//            "ts-assurance-service",
//            "ts-auth-service",
//            "ts-cancel-service",
//            "ts-config-service",
//            "ts-consign-service",
//            "ts-contacts-service",
//            "ts-delivery-service",
//            "ts-food-service",
//            "ts-gateway",
//            "ts-new-gateway",
//            "ts-notification-service",
//            "ts-order-related-service",
//            "ts-order-service",
//            "ts-preserve-service",
//            "ts-price-service",
//            "ts-rebook-service",
//            "ts-route-service",
//            "ts-security-service",
//            "ts-station-service",
//            "ts-travel-service",
//            "ts-ui-dashboard",
//            "ts-user-service");

    private final String label;
    private final String timestamp;
    private final boolean directed;
    private final boolean multigraph;
    private final Set<String> nodes;
    private final Set<Edge> edges;


    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("label", label);
        jsonObject.addProperty("timestamp", timestamp);
        jsonObject.addProperty("directed", directed);
        jsonObject.addProperty("multigraph", multigraph);
        jsonObject.addProperty("nodes", nodes.size());
        jsonObject.addProperty("edges", edges.size());

        return jsonObject;
    }
}
