package edu.university.ecs.lab.detection.models.results.architecture;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import edu.university.ecs.lab.detection.models.results.architecture.enums.Confidence;
import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.delta.models.Delta;
import lombok.Data;

/**
 * Architectural Rule 24 Class: No Health Checks Found
 */
@Data
public class AR24 extends AbstractAR{

    /**
     * Architectural rule 24 details
     */
    protected static final String TYPE = "Architectural Rule 24";
    protected static final String NAME = "No Health Checks Found";
    protected static final String DESC = "The lack of mechanisms for monitoring the health and availability of microservices, which can result in undetected failures and decreased system reliability.";
    protected static final Confidence CONFIDENCE = Confidence.UNKNOWN;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Scan and compare old microservice system and new microservice system to check for health check configuration
     * 
     * @param delta change between old commit and new microservice systems
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return 
     */
    public static List<AR24> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem){
        List<AR24> archRules = new ArrayList<>();

        if(delta.getConfigChange() == null){
            return archRules;
        }

        if (checkHealthcheck(delta, delta.getConfigChange(), oldSystem, newSystem) == null){
            archRules.add(checkHealthcheck(delta, delta.getConfigChange(), oldSystem, newSystem));
        }

        return archRules;
    }

    /**
     * Checks if both circuit breaker and rate limiter health checks are enabled in the YAML configuration.
     * 
     * @param delta change between old commit and new microservice systems
     * @param configFile The YAML file to check.
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return AR24 object if no health check configuration is found, null otherwise
     */
    public static AR24 checkHealthcheck(Delta delta, ConfigFile configFile, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        AR24 archRule24 = new AR24();

        if (configFile.getName().equals("application.yml") && configFile.getFileType().equals(FileType.CONFIG)){
            JsonNode data = configFile.getData();
            if (data != null){
                if (containsHealthCheck(data)){
                    return null;
                }
                else{
                    ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
                    jsonObject.put(" No Health Check Found:", true);
                    jsonObject.put("Change Type: ", delta.getChangeType().toString());
                    archRule24.setOldCommitID(oldSystem.getCommitID());
                    archRule24.setNewCommitID(newSystem.getCommitID());
                    archRule24.setMetaData(jsonObject);
                }
            }
        } else{
            return null;
        }

        return archRule24;
    }

    /**
     * Checks if the given JSON object contains the necessary configurations for health checks.
     * Specifically, it verifies if both circuit breaker and rate limiter health checks are enabled
     * and if the health indicators are registered for both circuit breakers and rate limiters.
     *
     * @param data The JsonObject representing the configuration data to check.
     * @return true if the necessary health check configurations are present and enabled, false otherwise.
     */
    private static boolean containsHealthCheck(JsonNode data){
        boolean healthCheckEnabled = false;
        boolean registerHealthIndicatorCB = false;
        boolean registerHealthIndicatorRL = false;

        if (data.has("management")) {
            JsonNode management = data.get("management");
            if (management.has("health")) {
                JsonNode health = management.get("health");
                if (health.has("circuitbreakers")) {
                    if (health.has("ratelimiters")) {
                        JsonNode ratelimiters = health.get("ratelimiters");
                        JsonNode circuitbreakers = health.get("circuitbreakers");
                        if (circuitbreakers.has("enabled") && circuitbreakers.get("enabled").asBoolean() &&
                            ratelimiters.has("enabled") && ratelimiters.get("enabled").asBoolean()) {
                            healthCheckEnabled = true;
                        }
                    }
                }
            }
        }

        if (data.has("resilience4j")) {
            JsonNode resilience4j = data.get("resilience4j");
            if (resilience4j.has("circuitbreaker")) {
                JsonNode circuitbreaker = resilience4j.get("circuitbreaker");
                if (circuitbreaker.has("configs")) {
                    JsonNode configs = circuitbreaker.get("configs");
                    if (configs.has("default")) {
                        JsonNode defaultConfig = configs.get("default");
                        if (defaultConfig.has("registerHealthIndicator") && defaultConfig.get("registerHealthIndicator").asBoolean()) {
                            registerHealthIndicatorCB = true;
                        }
                    }
                }
            }

            if (resilience4j.has("ratelimiter")) {
                JsonNode ratelimiter = resilience4j.get("ratelimiter");
                if (ratelimiter.has("configs")) {
                    JsonNode configs = ratelimiter.get("configs");
                    if (configs.has("instances")) {
                        JsonNode instances = configs.get("instances");
                        if (instances.has("registerHealthIndicator") && instances.get("registerHealthIndicator").asBoolean()) {
                            registerHealthIndicatorRL = true;
                        }
                    }
                }
            }
        }

        return healthCheckEnabled && registerHealthIndicatorCB && registerHealthIndicatorRL;
   }
}
