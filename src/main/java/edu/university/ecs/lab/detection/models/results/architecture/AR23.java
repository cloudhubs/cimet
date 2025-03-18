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
 * Architectureal Rule 23 Class: No API Gateway Found
 */
@Data
public class AR23 extends AbstractAR{

    /**
     * Architectural rule 23 details
     */
    protected static final String TYPE = "Architectural Rule 23";
    protected static final String NAME = "No API Gateway Found";
    protected static final String DESC = "The absence of a centralized entry point for managing, routing, and securing API calls, leading to potential inefficiencies and security vulnerabilities.";
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
     * Scan and compare old microservice system and new microservice system to check for API gateway
     * 
     * @param delta change between old commit and new microservice systems
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list with single AR23 object is no API gateway found, empty list otherwise
     */
    public static List<AR23> scan (Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem){
        List<AR23> archRules = new ArrayList<>();

        if(delta.getConfigChange() == null){
            return archRules;
        }

        if (checkforApiGateway(delta, delta.getConfigChange(), oldSystem, newSystem) == null){
            archRules.add(checkforApiGateway(delta, delta.getConfigChange(), oldSystem, newSystem));
        }

        return archRules;
    }

    /**
     * Checks if the YAML file contains configuration indicating an API Gateway.
     * 
     * @param delta change between old commit and new microservice systems
     * @param configFile The YAML file to check.
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return AR23 object for no API gateway, otherwise null
     */
    public static AR23 checkforApiGateway(Delta delta, ConfigFile configFile, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
       AR23 archRule23 = new AR23();

        if (configFile.getName().equals("application.yml") && configFile.getFileType().equals(FileType.CONFIG)){
            JsonNode data = configFile.getData();
                if (data != null) {
                    if (containsApiGatewayConfiguration(data)) {
                        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
                        jsonObject.put("No API Gateway:", false);
                        jsonObject.put("Change Type: ", delta.getChangeType().toString());
                        archRule23.setOldCommitID(oldSystem.getCommitID());
                        archRule23.setNewCommitID(newSystem.getCommitID());
                        archRule23.setMetaData(jsonObject);
                    } else{
                        return null;
                    }
                }
        } else {
            return null;
        }
        
        return archRule23;
    }

    /**
     * Checks if JSON object contains API gateway configuration details
     * 
     * @param data JSON object to check
     * @return true if API gateway configuration is found, false otherwise
     */   
    private static boolean containsApiGatewayConfiguration(JsonNode data) {
    if (data.has("spring")) {
        JsonNode spring = data.get("spring");
        if (spring.has("cloud")) {
            JsonNode cloud = spring.get("cloud");
            if (cloud.has("gateway")) {
                return true;
            }
        }
    }
    return false;
}
    
}
