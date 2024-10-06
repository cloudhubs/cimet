package edu.university.ecs.lab;

import edu.university.ecs.lab.detection.ExcelOutputRunner;
import java.io.File;
import java.io.IOException;

/**
 * Runs excel output runner for all config files in valid_configs directory
 * NOTE: Must change the ExcelOutputRunner class to take config filepath as input args
 */
public class AllConfigsExcelRunner {
    public static void main(String[] args) throws IOException {

        File configDir = new File("./valid_configs");
        if (!configDir.exists() || !configDir.isDirectory()) {
            System.out.println("Config directory './valid_configs' does not exist or is not a directory.");
            return;
        }

        File[] configFiles = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            System.out.println("No configuration files found in './valid_configs' directory.");
            return;
        }

        for (File configFile : configFiles) {
            String configPath = configFile.getAbsolutePath();
            System.out.println("Processing config file: " + configPath);
            ExcelOutputRunner.main(new String[]{configPath});
        }
    }
}
