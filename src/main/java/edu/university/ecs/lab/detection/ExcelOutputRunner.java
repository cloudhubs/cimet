package edu.university.ecs.lab.detection;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

/**
 * Runner class to execute detection service
 */
public class ExcelOutputRunner {

    public static void main(String[] args) throws IOException, InterruptedException, GitAPIException {
        String configPath = "./config.json";
        File conifgFile = new File(configPath);
        if (!conifgFile.exists()) {
            throw new FileNotFoundException();
        }

        DetectionService detectionService = new DetectionService(configPath);
        detectionService.runDetection();
    }

}
