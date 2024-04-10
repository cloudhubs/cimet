package edu.university.ecs.lab.impact.metrics;

import edu.university.ecs.lab.common.models.Flow;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.intermediate.merge.models.SystemChange;
import edu.university.ecs.lab.intermediate.merge.models.Delta;
import edu.university.ecs.lab.common.utils.IRParserUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static edu.university.ecs.lab.common.utils.FlowUtils.buildFlows;

public class MetricsRunner {
  static int totalChanges = 0;

  static Set<String> affectedClassLocalPaths = new HashSet<>();
  public static void main(String[] args) throws IOException {
//    if (args.length < 2) {
//      System.err.println(
//          "Invalid # of args, 2 expected: <path/to/old/intermediate-json>  <path/to/delta>");
//      return;
//    }

    Map<String, MsModel> systemMap = IRParserUtils.parseIRSystem(Path.of("out/rest-extraction-output-[1712174345434].json").toAbsolutePath().toString()).getServiceMap();
    SystemChange systemChange = IRParserUtils.parseDelta(Path.of("out/delta-changes-[1712174479531].json").toAbsolutePath().toString());

    // First changes to classes
    System.out.println("Controller changes: " + getChangeCount(systemMap, systemChange.getControllers(), ClassRole.CONTROLLER));
    System.out.println("Service changes: " + getChangeCount(systemMap, systemChange.getServices(), ClassRole.SERVICE));
    System.out.println("Repository changes: " + getChangeCount(systemMap, systemChange.getRepositories(), ClassRole.REPOSITORY));
    System.out.println("Dto changes: " + getChangeCount(systemMap, systemChange.getDtos(), ClassRole.DTO));
    System.out.println("Entity changes: " + getChangeCount(systemMap, systemChange.getEntities(), ClassRole.ENTITY));


    System.out.println(
            "% of Literal Classes Changed: "
                    + ((double) totalChanges / systemMap.values().stream().mapToInt(MsModel::getModelSize).sum()));

    updateAffectClassLocalPaths(systemMap, systemChange.getControllers(), ClassRole.CONTROLLER);
    updateAffectClassLocalPaths(systemMap, systemChange.getServices(), ClassRole.SERVICE);
    updateAffectClassLocalPaths(systemMap, systemChange.getRepositories(), ClassRole.REPOSITORY);


    System.out.println(
            "% of Affected Classes Changed (Flows): "
                    + ((double) affectedClassLocalPaths.size() / systemMap.values().stream().mapToInt(MsModel::getModelSize).sum()));
  }

  private static boolean verifyClassChange(List<? extends JClass> classList, Delta delta) {
    String msName = delta.getMsName();
    JClass jClassNew = delta.getChange();

    JClass matchingClass =
            classList.stream()
            .filter(c -> c.getClassPath().contains(msName))
            .findFirst()
            .orElse(null);

    if (matchingClass == null) {
      throw new RuntimeException("Error");
    }

    // Return true if not all attributes match
    return (!matchingClass.equals(jClassNew));
  }

  private static int getChangeCount(Map<String, MsModel> systemMap, List<Delta> changeList, ClassRole classRole) {
    int changes = 0;
    for (Delta delta : changeList) {
      String localPath = delta.getLocalPath();

      switch (delta.getChangeType()) {
        case "ADD":
          changes++;
          break;
        case "DELETE":
          changes++;
          break;
        case "MODIFY":
          MsModel currModel = systemMap.get(delta.getMsName());
          // todo
          if (Objects.isNull(currModel)) {
            throw new RuntimeException("ERROR");
          }

          if(verifyClassChange(getClassListFromRole(currModel, classRole), delta)) {
            changes++;
          }

          break;
      }
    }
    totalChanges += changes;
    return changes;
  }

  private static List<? extends JClass> getClassListFromRole(MsModel model, ClassRole classRole) {
    switch(classRole) {
      case CONTROLLER:
        return model.getControllers();
      case SERVICE:
        return model.getServices();
      case REPOSITORY:
        return model.getRepositories();
      case DTO:
        return model.getDtos();
      case ENTITY:
        return model.getEntities();
    }

    throw new RuntimeException("ERROR");
  }

  private static void updateAffectClassLocalPaths(Map<String, MsModel> systemMap, List<Delta> changeList, ClassRole classRole) {

    for(Delta delta : changeList) {
      MsModel currModel = systemMap.get(delta.getMsName());

      List<Flow> flows = buildFlows(currModel);

      for(Flow flow : flows) {
        switch(classRole) {
          case CONTROLLER:
            if (Objects.nonNull(flow.getController()) && flow.getController().getClassPath().equals(delta.getLocalPath()))
              affectedClassLocalPaths.addAll(getLocalClassPathsFromFlow(flow));
            break;
          case SERVICE:
            if (Objects.nonNull(flow.getService()) && flow.getService().getClassPath().equals(delta.getLocalPath()))
              affectedClassLocalPaths.addAll(getLocalClassPathsFromFlow(flow));
            break;
          case REPOSITORY:
            if (Objects.nonNull(flow.getRepository()) && flow.getRepository().getClassPath().equals(delta.getLocalPath()))
              affectedClassLocalPaths.addAll(getLocalClassPathsFromFlow(flow));
            break;
          case DTO:
            break;
          case ENTITY:
            break;
        }
      }
    }
  }

  private static List<String> getLocalClassPathsFromFlow(Flow flow) {
    return List.of(flow.getController().getClassPath(), flow.getService().getClassPath(), flow.getRepository().getClassPath());
  }
}
