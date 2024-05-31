package edu.university.ecs.lab.intermediate.create.services;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.models.InputRepository;
import edu.university.ecs.lab.common.models.JController;
import edu.university.ecs.lab.common.models.JService;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.Microservice;
import javassist.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Service for extracting REST endpoints and dependencies for a given microservice. */
public class RestModelService {

  /** The input configuration file */
  private final Config config;

  private final String baseBranch;
  private final String baseCommit;

  public RestModelService(String configPath, String baseBranch, String baseCommit) throws Exception {
    this.config = ConfigUtil.readConfig(configPath);
    this.baseBranch = config.getBranch();
    this.baseCommit = config.getInitialCommit();
  }

  /**
   * Recursively scan the files in the given repository path and extract the endpoints and
   * dependencies for a single microservice.
   *
   * @param inputRepo repository as described in the config file
   * @param localMicroservicePath the local path to the microservice directory
   * @throws NotFoundException if the service name is not found in the repository paths
   * @return model of a single service containing the extracted endpoints and dependencies
   */
  public Microservice recursivelyScanFiles(InputRepository inputRepo, String localMicroservicePath)
      throws NotFoundException {
    System.out.println("Scanning repository '" + localMicroservicePath + "'...");

    List<JClass> controllers = new ArrayList<>();
    List<JClass> services = new ArrayList<>();
    List<JClass> dtos = new ArrayList<>();
    List<JClass> repositories = new ArrayList<>();
    List<JClass> entities = new ArrayList<>();

    File localDir = new File(localMicroservicePath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      throw new NotFoundException("Invalid path given: " + localMicroservicePath);
    }

    scanDirectory(localDir, controllers, services, dtos, repositories, entities);

    String id = inputRepo.getServiceNameFromPath(localMicroservicePath);

    Microservice model =
        new Microservice(
            id, baseBranch, baseCommit, controllers, services, dtos, repositories, entities);

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
