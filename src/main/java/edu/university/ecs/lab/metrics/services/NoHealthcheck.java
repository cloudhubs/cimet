package edu.university.ecs.lab.metrics.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class NoHealthcheck {

    public boolean checkHealthcheck(String yamlFilePath) {
        try {
            // Read YAML file and convert to JSON
            String jsonContent = convertYamlToJson(yamlFilePath);

            // Check JSON content for any healthchecks
            boolean circuitBreakerEnabled = isCircuitBreakerEnabled(jsonContent);
            boolean rateLimiterEnabled = isRateLimiterEnabled(jsonContent);

            // Return true if any of the configurations are enabled (meaning there are healthchecks)
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

        // Check if the circuit breaker configuration is enabled
        JsonNode circuitBreakerNode = rootNode.path("management.health.circuitbreakers.enabled");
        return circuitBreakerNode.isBoolean() && circuitBreakerNode.asBoolean(false);
    }

    private boolean isRateLimiterEnabled(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);

        // Check if the rate limiter configuration is enabled
        JsonNode rateLimiterNode = rootNode.path("management.health.ratelimiters.enabled");
        return rateLimiterNode.isBoolean() && rateLimiterNode.asBoolean(false);
    }
}
