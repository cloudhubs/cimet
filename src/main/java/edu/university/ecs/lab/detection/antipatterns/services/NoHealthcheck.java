package edu.university.ecs.lab.detection.antipatterns.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class NoHealthcheck {

    public boolean checkHealthcheck(String yamlFilePath) {
        try {
            String jsonContent = convertYamlToJson(yamlFilePath);

            boolean circuitBreakerEnabled = isCircuitBreakerEnabled(jsonContent);
            boolean rateLimiterEnabled = isRateLimiterEnabled(jsonContent);
            return circuitBreakerEnabled && rateLimiterEnabled;

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

    private boolean isCircuitBreakerEnabled(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);

        JsonNode circuitBreakerNode = rootNode.path("management.health.circuitbreakers.enabled");
        return circuitBreakerNode.isBoolean() && circuitBreakerNode.asBoolean(false);
    }

    private boolean isRateLimiterEnabled(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);

        JsonNode rateLimiterNode = rootNode.path("management.health.ratelimiters.enabled");
        return rateLimiterNode.isBoolean() && rateLimiterNode.asBoolean(false);
    }
}
