package edu.university.ecs.lab.detection.antipatterns.services;

import java.util.Map;
import java.util.HashMap;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.detection.antipatterns.models.NoHealthcheck;


/**
* Service class to check the presence of health check configurations in a YAML file.
*/
public class NoHealthcheckService {

   /**
    * Checks if both circuit breaker and rate limiter health checks are enabled in the YAML configuration.
    * @return NoHealthcheck object that contains true if both circuit breaker
    * and rate limiter health checks are enabled, NoHealthcheck object that contains false otherwise.
    */
    public NoHealthcheck checkHealthcheck(MicroserviceSystem microserviceSystem) {
        Map<String, Boolean> noHealthCheckMap = new HashMap<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()){
            if (microservice.getFiles().isEmpty()){
                noHealthCheckMap.put(microservice.getName(), false);
            }
            for (ConfigFile configFile : microservice.getFiles()){
                if (configFile.getName().equals("application.yml") && configFile.getFileType().equals(FileType.CONFIG)){
                    JsonObject data = configFile.getData();
                    if (data != null){
                        if (containsHealthCheck(data)){
                            noHealthCheckMap.put(microservice.getName(), true);
                        }
                        else{
                            noHealthCheckMap.put(microservice.getName(), false);
                        }
                    }
                }
                else{
                    noHealthCheckMap.put(microservice.getName(), false);
                }
            }
        }

        return new NoHealthcheck(noHealthCheckMap);
   }

   /**
     * Checks if the given JSON object contains the necessary configurations for health checks.
     * Specifically, it verifies if both circuit breaker and rate limiter health checks are enabled
     * and if the health indicators are registered for both circuit breakers and rate limiters.
     *
     * @param data The JsonObject representing the configuration data to check.
     * @return true if the necessary health check configurations are present and enabled, false otherwise.
     */
    private boolean containsHealthCheck(JsonObject data){
        boolean healthCheckEnabled = false;
        boolean registerHealthIndicatorCB = false;
        boolean registerHealthIndicatorRL = false;

        // for health check: "management" object needs "health" with both "circuitbreakers" and "ratelimiters" enabled
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

        // for registered circuit breaker health indicator:
        //     "resilience4j" needs "circuitbreaker" -> "configs" -> "default" with "registerHealthIndicator" marked as true
        // for registered rate limiter health indicator:
        //     "ratelimiter" -> "configs" -> "instances" with "registerHealthIndicator" marked as true
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
