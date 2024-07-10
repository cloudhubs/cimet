//package edu.university.ecs.lab.detection.antipatterns.services;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import edu.university.ecs.lab.common.utils.NonJsonReadWriteUtils;
//import edu.university.ecs.lab.detection.antipatterns.models.NoHealthcheck;
//
//import java.io.IOException;
//
///**
// * Service class to check the presence of health check configurations in a YAML file.
// */
//public class NoHealthcheckService {
//
//    /**
//     * Checks if both circuit breaker and rate limiter health checks are enabled in the YAML configuration.
//     * @param yamlFilePath The path to the YAML file to check.
//     * @return NoHealthcheck object that contains true if both circuit breaker
//     * and rate limiter health checks are enabled, NoHealthcheck object that contains false otherwise.
//     */
//    public NoHealthcheck checkHealthcheck(String yamlFilePath) {
//        NoHealthcheck noHealthCheck = new NoHealthcheck(false);
//        try {
//            String jsonContent = NonJsonReadWriteUtils.readFromYaml(yamlFilePath);
//
//            boolean circuitBreakerEnabled = isCircuitBreakerEnabled(jsonContent);
//            boolean rateLimiterEnabled = isRateLimiterEnabled(jsonContent);
//            return noHealthCheck = new NoHealthcheck(circuitBreakerEnabled && rateLimiterEnabled);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return noHealthCheck;
//        }
//    }
//
//    /**
//     * Checks if the circuit breaker health check is enabled in the JSON content.
//     * @param jsonContent The JSON content to check.
//     * @return true if circuit breaker health check is enabled, false otherwise.
//     * @throws IOException If there is an error processing the JSON content.
//     */
//    private boolean isCircuitBreakerEnabled(String jsonContent) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode rootNode = mapper.readTree(jsonContent);
//
//        JsonNode circuitBreakerNode = rootNode.path("management.health.circuitbreakers.enabled");
//        return circuitBreakerNode.isBoolean() && circuitBreakerNode.asBoolean(false);
//    }
//
//    /**
//     * Checks if the rate limiter health check is enabled in the JSON content.
//     * @param jsonContent The JSON content to check.
//     * @return true if rate limiter health check is enabled, false otherwise.
//     * @throws IOException If there is an error processing the JSON content.
//     */
//    private boolean isRateLimiterEnabled(String jsonContent) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode rootNode = mapper.readTree(jsonContent);
//
//        JsonNode rateLimiterNode = rootNode.path("management.health.ratelimiters.enabled");
//        return rateLimiterNode.isBoolean() && rateLimiterNode.asBoolean(false);
//    }
//}
