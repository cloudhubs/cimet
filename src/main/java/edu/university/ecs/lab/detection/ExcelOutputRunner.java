package edu.university.ecs.lab.detection;

import java.io.IOException;

/**
 * Runner class to execute detection service
 */
public class ExcelOutputRunner {


    public static void main(String[] args) throws IOException {
        DetectionService detectionService = new DetectionService("./config.json");
        detectionService.runDetection();
    }

}
