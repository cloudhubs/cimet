package edu.university.ecs.lab.detection;
import java.io.IOException;

public class ExcelOutputRunner {


    public static void main(String[] args) throws IOException {
        String configPath = "./valid_configs/java-microservice.json";
        DetectionService detectionService = new DetectionService(configPath);
        detectionService.runDetection();
    }

}
