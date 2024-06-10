package edu.university.ecs.lab.intermediate.create.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import javassist.NotFoundException;

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

    // TODO REMOVE FOR TESTING ONLY
    public IRExtractionService(Config config) {
        gitService = new GitService(config);
        this.config = config;
    }

  /**
   * Intermediate extraction runner, generates IR from remote repository and writes to file.
   *
   * @return the name of the output file
   */
  public void generateIR() {
    // Clone remote repositories and scan through each cloned repo to extract endpoints
      List<Microservice> microservices = cloneAndScanServices();

    if (microservices.isEmpty()) {
      System.out.println("No microservices found");
    }

    // Scan through each endpoint to update rest call destinations
//    updateCallDestinations(msDataMap);

    //  Write each service and endpoints to IR
      writeToFile(microservices);

  }

  /**
   * Clone remote repositories and scan through each local repo and extract endpoints/calls
   *
   * @return a map of services and their endpoints
   */
  public List<Microservice> cloneAndScanServices() {
      List<Microservice> microservices = new ArrayList<>();

      // Clone the repository present in the configuration file
      gitService.cloneRemote();

      // Start scanning from the root directory
      List<String> rootDirectories = findRootDirectories(FileUtils.getClonePath(config.getRepoName()));

      // Scan each root directory for microservices
      for (String rootDirectory : rootDirectories) {
          Microservice microservice = recursivelyScanFiles(rootDirectory);
          if (microservice != null) {
              microservices.add(microservice);
          }
      }

      return microservices;
  }

    /**
     * Recursively search for directories containing a Dockerfile.
     *
     * @param directory the directory to start the search from
     * @return a list of directory paths containing a Dockerfile
     */
    private List<String> findRootDirectories(String directory) {
        List<String> rootDirectories = new ArrayList<>();
        File root = new File(directory);
        if (root.exists() && root.isDirectory()) {
            // Check if the current directory contains a Dockerfile
            File[] files = root.listFiles();
            boolean containsDockerfile = false;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().equals("Dockerfile") || file.getName().equals("pom.xml")) && !file.getParentFile().getName().equals(config.getRepoName())) {
                        containsDockerfile = true;
                        break;
                    }
                }
            }
            if (containsDockerfile) {
                rootDirectories.add(root.getPath());
                return rootDirectories;
            } else {
                // Recursively search for directories containing a Dockerfile
                for (File file : files) {
                    if (file.isDirectory()) {
                        rootDirectories.addAll(findRootDirectories(file.getPath()));
                    }
                }
            }
        }
        return rootDirectories;
    }


  /**
   * Write each service and endpoints to intermediate representation
   *
   * @param microservices a list of microservices extracted from repository
   */
  private void writeToFile(List<Microservice> microservices) {

    MicroserviceSystem microserviceSystem = new MicroserviceSystem(config.getSystemName(), MicroserviceSystem.INITIAL_VERSION, microservices);

    JsonReadWriteUtils.writeToJSON("./output/IR.json", microserviceSystem.toJsonObject());

    System.out.println("Successfully wrote rest extraction to: \"" + "IR.json" + "\"");
  }

  /**
   * Get name of output file for the IR
   *
   * @return the output file name
   */
  private String getOutputFileName() {
    return FileUtils.getBaseOutputPath()
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
        List<JClass> repositories = new ArrayList<>();

        scanDirectory(localDir, controllers, services, repositories);

        String id = FileUtils.getMicroserviceNameFromPath(rootMicroservicePath);

        Microservice model =
                new Microservice(
                        id, config.getBaseBranch(), config.getBaseCommit(), controllers, services, repositories);

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
            List<JClass> repositories) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, controllers, services, repositories);
                } else if (file.getName().endsWith(".java")) {
                    scanFile(file, controllers, services, repositories);
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
            List<JClass> repositories) {
        try {
            JClass jClass = SourceToObjectUtils.parseClass(file, config);
            
            if (jClass == null) {
                return;
            }

            //jClass.setClassPath(removeFirstTwoComponents(jClass.getClassPath()));
            // Switch through class roles and handle additional logic if needed
            switch (jClass.getClassRole()) {
                case CONTROLLER:
                    controllers.add(jClass);
                    break;
                case SERVICE:
                    services.add(jClass);
                    break;
                case REPOSITORY:
                    repositories.add(jClass);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            System.err.println("Could not parse file due to unrecognized type: " + e.getMessage());
        }
    }
}
