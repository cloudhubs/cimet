package edu.university.ecs.lab.intermediate.create.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import javassist.NotFoundException;

import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.util.*;


/**
 * Top-level service for extracting intermediate representation from remote repositories. Methods
 * are allowed to exit the program with an error code if an error occurs.
 */
public class IRExtractionService {
  /** Service to handle cloning from git */
  private final GitService gitService;

  private final Config config;

  /**
   * @param configPath path to configuration file
   */
  public IRExtractionService(String configPath) {
    gitService = new GitService(configPath);
    config = ConfigUtil.readConfig(configPath);
  }

  /**
   * Intermediate extraction runner, generates IR from remote repository and writes to file.
   *
   * @return the name of the output file
   */
  public String generateSystemIntermediate() {
    // Clone remote repositories and scan through each cloned repo to extract endpoints
    Map<String, Microservice> msDataMap = cloneAndScanServices();

    if (msDataMap == null) {
      Error.reportAndExit(Error.UNKNOWN_ERROR);
    }

    // Scan through each endpoint to update rest call destinations
//    updateCallDestinations(msDataMap);

    //  Write each service and endpoints to IR
    try {
      return this.writeToFile(msDataMap);
    } catch (IOException e) {
      Error.reportAndExit(Error.UNKNOWN_ERROR);
    }

    return null;
  }

  /**
   * Clone remote repositories and scan through each local repo and extract endpoints/calls
   *
   * @return a map of services and their endpoints
   */
  public Map<String, Microservice> cloneAndScanServices() {
    Map<String, Microservice> msModelMap = new HashMap<>();

    // Clone the repository present in the configuration file
    gitService.cloneRemote();


    // Get list of paths to local microservices as "./cloneDir/.../msName"
  // Scan through each local repo and extract endpoints/calls
  for (String rootMicroservicePath : config.getMicroservicePaths()) {
    // Bulk of the work extracting the microservice from the cloned files
    Microservice microservice = null;

    microservice = recursivelyScanFiles(rootMicroservicePath);

//    // Remove clonePath from path
//    String path = msPath;
//    if (msPath.contains(basePath) && msPath.length() > basePath.length() + 1) {
//      path = msPath.substring(basePath.length() + 1);
//    }

    msModelMap.put(rootMicroservicePath, microservice);
  }


    return msModelMap;
  }


  /**
   * Write each service and endpoints to intermediate representation
   *
   * @param msMap a map of service to their information
   */
  private String writeToFile(Map<String, Microservice> msMap) throws IOException {
    String outputName = this.getOutputFileName();

    JsonObject jout = new edu.university.ecs.lab.common.models.System(config.getSystemName(), "1", new ArrayList<>(msMap.values())).toJsonObject();

    JsonReadWriteUtils.writeJsonToFile(jout, outputName);

    System.out.println("Successfully wrote rest extraction to: \"" + outputName + "\"");
    return outputName;
  }

  /**
   * Get name of output file for the IR
   *
   * @return the output file name
   */
  private String getOutputFileName() {
    return ConfigUtil.getBaseOutputPath()
        + "/rest-extraction-output-["
        + config.getBaseBranch()
        + "-"
        + config.getBaseCommit().substring(0, 7)
        + "].json";
  }

    /**
     * Recursively scan the files in the given repository path and extract the endpoints and
     * dependencies for a single microservice.
     *
     * @throws NotFoundException if the service name is not found in the repository paths
     * @return model of a single service containing the extracted endpoints and dependencies
     */
    public Microservice recursivelyScanFiles(String rootMicroservicePath) {
//        System.out.println("Scanning repository '" + rootMicroservicePath + "'...");

        // Validate path exists and is a directory
        File localDir = new File(rootMicroservicePath);
        if (!localDir.exists() || !localDir.isDirectory()) {
            Error.reportAndExit(Error.UNKNOWN_ERROR);
        }

        List<JClass> controllers = new ArrayList<>();
        List<JClass> services = new ArrayList<>();
        List<JClass> dtos = new ArrayList<>();
        List<JClass> repositories = new ArrayList<>();
        List<JClass> entities = new ArrayList<>();

        scanDirectory(localDir, controllers, services, dtos, repositories, entities);

        String id = ConfigUtil.getMicroserviceNameFromPath(rootMicroservicePath);

        Microservice model =
                new Microservice(
                        id, config.getBaseBranch(), config.getBaseCommit(), controllers, services, dtos, repositories, entities);

        System.out.println("Done!");
        return model;
    }

    /**
     * Recursively scan the given directory for files and extract the endpoints and dependencies.
     *
     * @param directory the directory to scan
     */
    public void scanDirectory(
            File directory,
            List<JClass> controllers,
            List<JClass> services,
            List<JClass> dtos,
            List<JClass> repositories,
            List<JClass> entities) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, controllers, services, dtos, repositories, entities);
                } else if (file.getName().endsWith(".java")) {
                    scanFile(file, controllers, services, dtos, repositories, entities);
                }
            }
        }
    }

    /**
     * Scan the given file for endpoints and calls to other services.
     *
     * @param file the file to scan
     * @apiNote CURRENT LIMITATION: We detect controllers/services/dtos/repositories/entities based on
     *     literally having that string within the file name. This is a naive approach and should be
     *     improved.
     */
    public void scanFile(
            File file,
            List<JClass> controllers,
            List<JClass> services,
            List<JClass> dtos,
            List<JClass> repositories,
            List<JClass> entities) {
        try {
            JClass jClass = SourceToObjectUtils.parseClass(file, config);

            if (jClass == null) {
                return;
            }

            // Switch through class roles and handle additional logic if needed
            switch (jClass.getClassRole()) {
                case CONTROLLER:
                    controllers.add(jClass);
                    break;
                case SERVICE:
                    services.add(jClass);
                    break;
                case DTO:
                    dtos.add(jClass);
                    break;
                case REPOSITORY:
                    repositories.add(jClass);
                    break;
                case ENTITY:
                    entities.add(jClass);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            System.err.println("Could not parse file due to unrecognized type: " + e.getMessage());
        }
    }
}
