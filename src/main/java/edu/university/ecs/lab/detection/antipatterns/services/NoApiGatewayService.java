package edu.university.ecs.lab.detection.antipatterns.services;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.detection.antipatterns.models.NoApiGateway;

/**
* Service class to detect the presence of an API Gateway configuration in a YAML file.
*/
public class NoApiGatewayService {

   /**
    * Checks if the YAML file contains configuration indicating an API Gateway.
    * @return NoApiGateway object that contains true if an API Gateway configuration is detected,
    * NoApiGateway object that contains false otherwise.
    */
   public NoApiGateway checkforApiGateway(MicroserviceSystem microserviceSystem) {
       NoApiGateway noApiGateway = new NoApiGateway(true);

       for (Microservice microservice : microserviceSystem.getMicroservices()){
        for (ConfigFile configFile : microservice.getFiles()){
            if (configFile.getName().equals("application.yml") && configFile.getFileType().equals(FileType.CONFIG)){
                JsonObject data = configFile.getData();
                    if (data != null) {
                        if (containsApiGatewayConfiguration(data)) {
                            noApiGateway = new NoApiGateway(false);
                            return noApiGateway;
                        }
                    }
            }
        }
       }

       return noApiGateway;
   }

   /**
     * Checks if the given JsonObject contains the "cloud" or "gateway" configuration.
     * @param data The JsonObject to check.
     * @return true if the configuration is found, false otherwise.
     */
    private boolean containsApiGatewayConfiguration(JsonObject data) {
        if (data.has("spring")) {
            JsonObject spring = data.getAsJsonObject("spring");
            if (spring.has("cloud")) {
                JsonObject cloud = spring.getAsJsonObject("cloud");
                if (cloud.has("gateway")) {
                    return true;
                }
            }
        }
        return false;
    }

}
