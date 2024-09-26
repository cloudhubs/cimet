package edu.university.ecs.lab.detection;

import java.io.IOException;

/**
 * Runner class to execute detection service
 */
public class ExcelOutputRunner {


    public static void main(String[] args) throws IOException {
        String configPath = "./config.json";
        DetectionService detectionService = new DetectionService(configPath);
        detectionService.runDetection();
    }

}
