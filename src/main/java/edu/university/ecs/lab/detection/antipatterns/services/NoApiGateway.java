package edu.university.ecs.lab.detection.antipatterns.services;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class NoApiGateway {

    public boolean checkforApiGateway(String yamlFilePath) {
        try {
            String jsonContent = convertYamlToJson(yamlFilePath);
            return isApiGateway(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String convertYamlToJson(String yamlFilePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        JsonNode yamlNode = yamlMapper.readTree(new File(yamlFilePath));
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.writeValueAsString(yamlNode);
    }

    private boolean isApiGateway(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);
    
        if (rootNode.has("spring") || rootNode.has("cloud")) {
            JsonNode gatewayNode = (rootNode.has("spring")) ? rootNode.get("spring").get("cloud").get("gateway") : rootNode.get("cloud").get("gateway");
    
            if (gatewayNode != null && gatewayNode.has("routes")) {
                JsonNode routesNode = gatewayNode.get("routes");
    
                for (JsonNode routeNode : routesNode) {
                    if (routeNode.has("id") && routeNode.has("uri") && routeNode.has("predicates")) {
                        return true;
                    }
                }
            }
        }
    
        return false;
    }
    
}
