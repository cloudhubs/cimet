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

@Data
public class AR23 extends AbstractAR{
    protected static final String TYPE = "Architectural Rule 23";
    protected static final String NAME = "No API Gateway Found";
    protected static final String DESC = "The absence of a centralized entry point for managing, routing, and securing API calls, leading to potential inefficiencies and security vulnerabilities.";
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

    public static AR23 checkforApiGateway(Delta delta, ConfigFile configFile, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
       AR23 archRule23 = new AR23();

        if (configFile.getName().equals("application.yml") && configFile.getFileType().equals(FileType.CONFIG)){
            JsonObject data = configFile.getData();
                if (data != null) {
                    if (containsApiGatewayConfiguration(data)) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("No API Gateway:", false);
                        jsonObject.addProperty("Change Type: ", delta.getChangeType().toString());
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

       

   private static boolean containsApiGatewayConfiguration(JsonObject data) {
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
