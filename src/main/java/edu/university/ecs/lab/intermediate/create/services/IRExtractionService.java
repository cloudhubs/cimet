package edu.university.ecs.lab.intermediate.create.services;

import edu.university.ecs.lab.common.config.models.InputRepository;
import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import javassist.NotFoundException;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.*;

import static edu.university.ecs.lab.common.models.enums.ErrorCodes.*;
import static edu.university.ecs.lab.intermediate.utils.IRParserUtils.updateCallDestinations;

/**
 * Top-level service for extracting intermediate representation from remote repositories. Methods
 * are allowed to exit the program with an error code if an error occurs.
 */
public class IRExtractionService {
  /** Service to handle cloning from git */
  private final GitService gitService;

  /** Service to handle parsing a git repo and extracting files */
  private final RestModelService restModelService;

  /**
   * @param config
   */
  public IRExtractionService(String configPath, String baseBranch, String baseCommit) throws Exception {
    gitService = new GitService(configPath);
    restModelService = new RestModelService(configPath, baseBranch, baseCommit);
  }

  /**
   * Intermediate extraction runner, generates IR from remote repository and writes to file.
   *
   * @return the name of the output file
   */
  public String generateSystemIntermediate() {
    // Clone remote repositories and scan through each cloned repo to extract endpoints
    Map<String, Microservice> msDataMap = this.cloneAndScanServices();

    if (msDataMap == null) {
      System.out.println(NO_MICROSERVICES.getMessage());
      System.exit(NO_MICROSERVICES.ordinal());
    }

    // Scan through each endpoint to update rest call destinations
    updateCallDestinations(msDataMap);

    //  Write each service and endpoints to IR
    try {
      return this.writeToFile(msDataMap);
    } catch (IOException e) {
      System.err.println("Error writing to IR json: " + e.getMessage());
      System.exit(JSON_FILE_WRITE_ERROR.ordinal());
      return null;
    }
  }

  /**
   * Clone remote repositories and scan through each local repo and extract endpoints/calls
   *
   * @return a map of services and their endpoints
   */
  public Map<String, Microservice> cloneAndScanServices() {
    Map<String, Microservice> msModelMap = new HashMap<>();

    this.validateOrCreateLocalDirectory(basePath);

    // For each git repository in the config file, clone the repository and scan through the
    // downloaded repo
    // for the microservices as per the structure in the input config file
    for (InputRepository inputRepository : config.getRepositories()) {
      // Clone the remote repository
      try {
        gitService.cloneRemote(inputRepository, baseCommit);
      } catch (Exception e) {
        System.err.println(GIT_CLONE_FAILED.getMessage() + ": " + e.getMessage());
        System.exit(GIT_CLONE_FAILED.ordinal());
      }

      // Get list of paths to local microservices as "./cloneDir/.../msName"
      List<String> microservicePaths = config.getMicroservicePaths(inputRepository);

      // Scan through each local repo and extract endpoints/calls
      for (String msPath : microservicePaths) {
        // Bulk of the work extracting the microservice from the cloned files
        Microservice model = null;
        try {
          model = restModelService.recursivelyScanFiles(inputRepository, msPath);
        } catch (NotFoundException e) {
          System.err.println("Error scanning repository: " + msPath);
          System.exit(IR_EXTRACTION_FAIL.ordinal());
        }

        // Remove clonePath from path
        String path = msPath;
        if (msPath.contains(basePath) && msPath.length() > basePath.length() + 1) {
          path = msPath.substring(basePath.length() + 1);
        }

        msModelMap.put(path, model);
      }
    }

    return msModelMap;
  }

//  /**
//   * Create local directory to clone files to ("./repos" by default) as specified in the config
//   * file. <div><br>
//   * Exits the program if the directory cannot be created.</div>
//   *
//   * <p><div><br>
//   * Post condition: Directory is created.</div>
//   */
//  private void validateOrCreateLocalDirectory() {
//    File cloneDir = new File(config);
//    if (!cloneDir.exists()) {
//      if (cloneDir.mkdirs()) {
//        System.out.println("Successfully created \"" + dirPath + "\" directory.");
//      } else {
//        System.err.println(COULD_NOT_CREATE_DIRECTORY.getMessage() + ": \"" + dirPath + "\"");
//        System.exit(COULD_NOT_CREATE_DIRECTORY.ordinal());
//      }
//    }
//  }

  /**
   * Write each service and endpoints to intermediate representation
   *
   * @param msMap a map of service to their information
   */
  private String writeToFile(Map<String, Microservice> msMap) throws IOException {
    String outputName = this.getOutputFileName();

    validateOrCreateLocalDirectory(config.getOutputPath());

    JsonObject jout =
        new MsSystem(config.getSystemName(), INIT_VERSION_NUMBER, new ArrayList<>(msMap.values()))
            .toJsonObject();

    MsJsonWriter.writeJsonToFile(jout, outputName);

    System.out.println("Successfully wrote rest extraction to: \"" + outputName + "\"");
    return outputName;
  }

  /**
   * Get name of output file for the IR
   *
   * @return the output file name
   */
  private String getOutputFileName() {
    return config.getOutputPath()
        + "/rest-extraction-output-["
        + baseBranch
        + "-"
        + baseCommit.substring(0, 7)
        + "].json";
  }
}
