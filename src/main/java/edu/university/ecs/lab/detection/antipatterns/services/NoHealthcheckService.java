package edu.university.ecs.lab.detection.antipatterns.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import edu.university.ecs.lab.detection.antipatterns.models.NoHealthcheck;

import java.io.File;
import java.io.IOException;

/**
 * Service class to check the presence of health check configurations in a YAML file.
 */
public class NoHealthcheckService {

    /**
     * Checks if both circuit breaker and rate limiter health checks are enabled in the YAML configuration.
     * @param yamlFilePath The path to the YAML file to check.
     * @return NoHealthcheck object that contains true if both circuit breaker 
     * and rate limiter health checks are enabled, NoHealthcheck object that contains false otherwise.
     */
    public NoHealthcheck checkHealthcheck(String yamlFilePath) {
        NoHealthcheck noHealthCheck = new NoHealthcheck(false);
        try {
            String jsonContent = convertYamlToJson(yamlFilePath);

            boolean circuitBreakerEnabled = isCircuitBreakerEnabled(jsonContent);
            boolean rateLimiterEnabled = isRateLimiterEnabled(jsonContent);
            return noHealthCheck = new NoHealthcheck(circuitBreakerEnabled && rateLimiterEnabled);

        } catch (IOException e) {
            e.printStackTrace();
            return noHealthCheck;
        }
    }

    /**
     * Converts YAML content from a file to JSON format.
     * @param yamlFilePath The path to the YAML file.
     * @return JSON string representation of the YAML content.
     * @throws IOException If there is an error reading the YAML file.
     */
    private String convertYamlToJson(String yamlFilePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        JsonNode yamlNode = yamlMapper.readTree(new File(yamlFilePath));
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.writeValueAsString(yamlNode);
    }

    /**
     * Checks if the circuit breaker health check is enabled in the JSON content.
     * @param jsonContent The JSON content to check.
     * @return true if circuit breaker health check is enabled, false otherwise.
     * @throws IOException If there is an error processing the JSON content.
     */
    private boolean isCircuitBreakerEnabled(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);

        JsonNode circuitBreakerNode = rootNode.path("management.health.circuitbreakers.enabled");
        return circuitBreakerNode.isBoolean() && circuitBreakerNode.asBoolean(false);
    }

    /**
     * Checks if the rate limiter health check is enabled in the JSON content.
     * @param jsonContent The JSON content to check.
     * @return true if rate limiter health check is enabled, false otherwise.
     * @throws IOException If there is an error processing the JSON content.
     */
    private boolean isRateLimiterEnabled(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);

        JsonNode rateLimiterNode = rootNode.path("management.health.ratelimiters.enabled");
        return rateLimiterNode.isBoolean() && rateLimiterNode.asBoolean(false);
    }
}
