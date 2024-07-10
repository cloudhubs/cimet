//package edu.university.ecs.lab.detection.antipatterns.services;
//
//import java.io.IOException;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import edu.university.ecs.lab.common.utils.NonJsonReadWriteUtils;
//import edu.university.ecs.lab.detection.antipatterns.models.NoApiGateway;
//
///**
// * Service class to detect the presence of an API Gateway configuration in a YAML file.
// */
//public class NoApiGatewayService {
//
//    /**
//     * Checks if the YAML file contains configuration indicating an API Gateway.
//     * @param yamlFilePath The path to the YAML file to check.
//     * @return NoApiGateway object that contains true if an API Gateway configuration is detected,
//     * NoApiGateway object that contains false otherwise.
//     */
//    public NoApiGateway checkforApiGateway(String yamlFilePath) {
//        NoApiGateway noApiGateway = new NoApiGateway(false);
//        try {
//            String jsonContent = NonJsonReadWriteUtils.readFromYaml(yamlFilePath);
//            return noApiGateway = new NoApiGateway(isApiGateway(jsonContent));
//        } catch (IOException e) {
//            e.printStackTrace();
//            return noApiGateway;
//        }
//    }
//
//    /**
//     * Checks if the provided JSON content indicates the presence of an API Gateway.
//     * @param jsonContent The JSON content to check.
//     * @return true if API Gateway configuration is detected, false otherwise.
//     * @throws IOException If there is an error processing the JSON content.
//     */
//    private boolean isApiGateway(String jsonContent) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode rootNode = mapper.readTree(jsonContent);
//
//        if (rootNode.has("spring") || rootNode.has("cloud")) {
//            JsonNode gatewayNode = (rootNode.has("spring")) ? rootNode.get("spring").get("cloud").get("gateway") : rootNode.get("cloud").get("gateway");
//
//            if (gatewayNode != null && gatewayNode.has("routes")) {
//                JsonNode routesNode = gatewayNode.get("routes");
//
//                for (JsonNode routeNode : routesNode) {
//                    if (routeNode.has("id") && routeNode.has("uri") && routeNode.has("predicates")) {
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
//
//}
