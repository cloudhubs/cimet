package edu.university.ecs.lab.detection;
import java.io.IOException;

public class ExcelOutputRunner {


    public static void main(String[] args) throws IOException {
        String configPath = args[0];
        DetectionService detectionService = new DetectionService(configPath);
        detectionService.runDetection();
    }

}
