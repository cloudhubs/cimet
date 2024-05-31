package edu.university.ecs.lab.intermediate.merge.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.utils.IRParserUtils;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;

import javax.json.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static edu.university.ecs.lab.common.models.enums.ErrorCodes.JSON_FILE_WRITE_ERROR;

public class MergeService {
  /** Path from working directory to intermediate file */
  private final String intermediatePath;

  /** Path from working directory to delta file */
  private final String deltaPath;

  private final String compareBranch;
  private final String compareCommit;

  private final Config config;

  private final MsSystem msSystem;
  private final SystemChange systemChange;
  private final Map<String, Microservice> msModelMap;

  // TODO handle exceptions here
  public MergeService(
      String intermediatePath,
      String deltaPath,
      Config config,
      String compareBranch,
      String compareCommit)
      throws IOException {
    this.intermediatePath = intermediatePath;
    this.deltaPath = deltaPath;
    this.config = config;
    this.msSystem =
        IRParserUtils.parseIRSystem(Path.of(intermediatePath).toAbsolutePath().toString());
    this.msModelMap = msSystem.getServiceMap();

    this.systemChange =
        IRParserUtils.parseSystemChange(Path.of(deltaPath).toAbsolutePath().toString());
    this.compareBranch = compareBranch;
    this.compareCommit = compareCommit;
  }

  public String mergeAndWriteToFile() {

    updateModelMap(ClassRole.CONTROLLER, systemChange.getControllers());
    updateModelMap(ClassRole.SERVICE, systemChange.getServices());
    updateModelMap(ClassRole.REPOSITORY, systemChange.getRepositories());
    updateModelMap(ClassRole.DTO, systemChange.getDtos());
    updateModelMap(ClassRole.ENTITY, systemChange.getEntities());

    // increment system version
    msSystem.incrementVersion();

    // save new system representation
    String outputFile = null;
    try {
      outputFile = writeNewIntermediate();
    } catch (IOException e) {
      System.err.println("Failed to write new IR from merge service: " + e.getMessage());
      System.exit(JSON_FILE_WRITE_ERROR.ordinal());
    }
    return outputFile;
  }

  private String writeNewIntermediate() throws IOException {

    JsonObject jout = msSystem.toJsonObject();

    String outputPath = config.getOutputPath();

    String outputName =
        outputPath
            + "/rest-extraction-new-["
            + compareBranch
            + "-"
            + compareCommit.substring(0, 7)
            + "].json";

    MsJsonWriter.writeJsonToFile(jout, outputName);
    System.out.println("Successfully wrote updated extraction to: \"" + outputName + "\"");
    return outputName;
  }

  // TODO this cannot handle file moves, only add/modify/delete
  private void updateModelMap(ClassRole classRole, Map<String, Delta> changeMap) {

    for (Delta delta : changeMap.values()) {
      String msId = delta.getMsId();

      // check change type
      switch (delta.getChangeType()) {
        case ADD:
          // Add new service or add to existing service
          addNewFiles(classRole, msId, delta);
          break;
        case DELETE:
          removeFiles(classRole, msId, delta);
          break;
        case MODIFY:
          modifyExisting(classRole, msId, delta);
          break;
        default:
          System.err.println(
              "Warning in merge service: Not yet implemented change type, skipping: "
                  + delta.getChangeType());
          break;
      }
    }
  }

  public void addNewFiles(ClassRole classRole, String msId, Delta delta) {

    // Check if service exists or if this is an entirely new service
    Microservice msModel;
    if (msModelMap.containsKey(msId)) {
      msModel = msModelMap.get(msId);
    } else {
      msModel = new Microservice(msId, delta.getCommitId());
    }

    if (classRole == ClassRole.SERVICE) {
      updateApiDestinationsAdd((JService) delta.getChangedClass(), msId);
    }

    msModel.addChange(delta);
    msModelMap.put(msId, msModel);
  }

  public void modifyExisting(ClassRole classRole, String msId, Delta delta) {
    if (!msModelMap.containsKey(msId)) {
      System.err.println(
          "Warning in merge service: Could not find service for MODIFY "
              + "(service should ideally exist for this type), skipping: "
              + msId);
      return;
    }

    // modification is simply file removal then an add
    removeFiles(classRole, msId, delta);
    addNewFiles(classRole, msId, delta);
  }

  public void removeFiles(ClassRole classRole, String msId, Delta delta) {
    Microservice msModel;

    if (msModelMap.containsKey(msId)) {
      msModel = msModelMap.get(msId);
    } else {
      msModel = new Microservice(msId, delta.getCommitId());
    }

    if (ClassRole.CONTROLLER == classRole) {
      JController controller =
          msModel.getControllers().stream()
              .filter(jController -> jController.matchClassPath(delta.getChangedClass()))
              .findFirst()
              .orElse(null);
      if (Objects.nonNull(controller)) {
        updateApiDestinationsDelete(controller, msId);
      }
    }

    removeIfClassMatches(msModel.getListForRole(classRole), delta);
  }

  private static <T extends JClass> void removeIfClassMatches(List<T> list, Delta delta) {
    list.removeIf(jClass -> jClass.matchClassPath(delta.getChangedClass()));
  }

  private void updateApiDestinationsAdd(JService service, String servicePath) {
    for (RestCall restCall : service.getRestCalls()) {
      for (Microservice ms : msModelMap.values()) {
        if (!ms.getId().equals(servicePath)) {
          for (JController controller : ms.getControllers()) {
            // Reassign controller if it is in the deltas
            String classPath = controller.getClassPath();
            JController deltaController =
                systemChange.getControllers().values().stream()
                    .filter(delta -> delta.getChangedClass().getClassPath().equals(classPath))
                    .map(delta -> (JController) delta.getChangedClass())
                    .findFirst()
                    .orElse(null);

            if (Objects.nonNull(deltaController)) {
              controller = deltaController;
            }

            for (Endpoint endpoint : controller.getEndpoints()) {
              if (endpoint.matchCall(restCall)) {
                restCall.setDestination(controller);
                endpoint.addCall(restCall, service);
              }
            }
          }
        }
      }
    }
  }

  private void updateApiDestinationsDelete(JController controller, String servicePath) {
    for (Endpoint endpoint : controller.getEndpoints()) {
      for (Microservice ms : msModelMap.values()) {
        if (!ms.getId().equals(servicePath)) {
          for (JService service : ms.getServices()) {
            for (RestCall restCall : service.getRestCalls()) {
              if (endpoint.matchCall(restCall)
                  && !restCall.pointsToDeletedFile()
                  && !"".equals(restCall.getDestFile())) {
                restCall.setDestinationAsDeleted();
              }
            }
          }
        }
      }
    }
  }
}
