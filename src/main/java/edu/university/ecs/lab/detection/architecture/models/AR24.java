package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.detection.architecture.models.enums.Confidence;
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
    
    private String oldCommitID;
    private String newCommitID;
    protected JsonObject metaData;

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

    @Override
    public JsonObject getMetaData() {
        return metaData;
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
            JsonObject data = configFile.getData();
            if (data != null){
                if (containsHealthCheck(data)){
                    return null;
                }
                else{
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(" No Health Check Found:", true);
                    jsonObject.addProperty("Change Type: ", delta.getChangeType().toString());
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
    private static boolean containsHealthCheck(JsonObject data){
        boolean healthCheckEnabled = false;
        boolean registerHealthIndicatorCB = false;
        boolean registerHealthIndicatorRL = false;

        if (data.has("management")) {
            JsonObject management = data.getAsJsonObject("management");
            if (management.has("health")) {
                JsonObject health = management.getAsJsonObject("health");
                if (health.has("circuitbreakers")) {
                    if (health.has("ratelimiters")) {
                        JsonObject ratelimiters = health.getAsJsonObject("ratelimiters");
                        JsonObject circuitbreakers = health.getAsJsonObject("circuitbreakers");
                        if (circuitbreakers.has("enabled") && circuitbreakers.get("enabled").getAsBoolean() &&
                            ratelimiters.has("enabled") && ratelimiters.get("enabled").getAsBoolean()) {
                            healthCheckEnabled = true;
                        }
                    }
                }
            }
        }

        if (data.has("resilience4j")) {
            JsonObject resilience4j = data.getAsJsonObject("resilience4j");
            if (resilience4j.has("circuitbreaker")) {
                JsonObject circuitbreaker = resilience4j.getAsJsonObject("circuitbreaker");
                if (circuitbreaker.has("configs")) {
                    JsonObject configs = circuitbreaker.getAsJsonObject("configs");
                    if (configs.has("default")) {
                        JsonObject defaultConfig = configs.getAsJsonObject("default");
                        if (defaultConfig.has("registerHealthIndicator") && defaultConfig.get("registerHealthIndicator").getAsBoolean()) {
                            registerHealthIndicatorCB = true;
                        }
                    }
                }
            }

            if (resilience4j.has("ratelimiter")) {
                JsonObject ratelimiter = resilience4j.getAsJsonObject("ratelimiter");
                if (ratelimiter.has("configs")) {
                    JsonObject configs = ratelimiter.getAsJsonObject("configs");
                    if (configs.has("instances")) {
                        JsonObject instances = configs.getAsJsonObject("instances");
                        if (instances.has("registerHealthIndicator") && instances.get("registerHealthIndicator").getAsBoolean()) {
                            registerHealthIndicatorRL = true;
                        }
                    }
                }
            }
        }

        return healthCheckEnabled && registerHealthIndicatorCB && registerHealthIndicatorRL;
   }
}
