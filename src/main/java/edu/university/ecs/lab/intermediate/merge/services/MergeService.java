 package edu.university.ecs.lab.intermediate.merge.services;

 import edu.university.ecs.lab.common.config.Config;
 import edu.university.ecs.lab.common.config.ConfigUtil;
 import edu.university.ecs.lab.common.error.Error;
 import edu.university.ecs.lab.common.models.*;
 import edu.university.ecs.lab.common.models.enums.ClassRole;
 import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
 import edu.university.ecs.lab.delta.models.Delta;
 import edu.university.ecs.lab.delta.models.SystemChange;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;


 public class MergeService {
  private final Config config;
  private final MicroserviceSystem microserviceSystem;
  private final SystemChange systemChange;

  // TODO handle exceptions here
  public MergeService(
      String intermediatePath,
      String deltaPath,
      String configPath) {
    this.config = ConfigUtil.readConfig(configPath);
    this.microserviceSystem = JsonReadWriteUtils.readFromJSON(Path.of(intermediatePath).toAbsolutePath().toString(), MicroserviceSystem.class);
    this.systemChange = JsonReadWriteUtils.readFromJSON(Path.of(deltaPath).toAbsolutePath().toString(), SystemChange.class);
  }

  public void makeAllChanges() {
        for(Delta d : systemChange.getChanges()) {
            switch (d.getChangeType()) {
                case ADD:
                    addNewFiles(d);
                    break;
                case MODIFY:
                    modifyFiles(d);
                    break;
                case DELETE:
                    removeFiles(d);
                    break;
            }
        }

        JsonReadWriteUtils.writeToJSON("./output/IR.json", microserviceSystem);
  }

    private JClass findFile(Delta delta) {
      for(Microservice microservice : microserviceSystem.getMicroservices()) {
          switch(delta.getChangedClass().getClassRole()) {
              case CONTROLLER:
                  return microservice.getControllers().stream().filter(jClass -> jClass.getClassPath().equals(delta.getChangedClass().getClassPath())).findFirst().orElse(null);
              case SERVICE:
                  return microservice.getServices().stream().filter(jClass -> jClass.getClassPath().equals(delta.getChangedClass().getClassPath())).findFirst().orElse(null);
              case REPOSITORY:
                  return microservice.getRepositories().stream().filter(jClass -> jClass.getClassPath().equals(delta.getChangedClass().getClassPath())).findFirst().orElse(null);

          }
      }

      return null;
    }


    public void modifyFiles(Delta delta) {
         Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(delta.getMicroserviceName())).findFirst().orElse(null);

         if(Objects.isNull(ms)) {
             Error.reportAndExit(Error.UNKNOWN_ERROR);
         }
         removeFiles(delta);
         addNewFiles(delta);

    }

  public void addNewFiles(Delta delta) {
      Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(delta.getMicroserviceName())).findFirst().orElse(null);

      if(Objects.isNull(ms)) {
          Error.reportAndExit(Error.UNKNOWN_ERROR);
      }


      switch(delta.getChangedClass().getClassRole()) {
          case CONTROLLER:
               ms.getControllers().add(delta.getChangedClass());
               break;
          case SERVICE:
              ms.getServices().add(delta.getChangedClass());
              break;
          case REPOSITORY:
              ms.getRepositories().add(delta.getChangedClass());
              break;

      }
      updateMicroserviceSystem(ms);

  }

    public void removeFiles(Delta delta) {
        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(delta.getMicroserviceName())).findFirst().orElse(null);

        if(Objects.isNull(ms)) {
            Error.reportAndExit(Error.UNKNOWN_ERROR);
        }


        switch(delta.getChangedClass().getClassRole()) {
            case CONTROLLER:
                ms.getControllers().removeIf(jClass -> jClass.getClassPath().equals(delta.getLocalPath()));
                break;
            case SERVICE:
                ms.getServices().removeIf(jClass -> jClass.getClassPath().equals(delta.getLocalPath()));
                break;
            case REPOSITORY:
                ms.getRepositories().removeIf(jClass -> jClass.getClassPath().equals(delta.getLocalPath()));
                break;

        }

        updateMicroserviceSystem(ms);

    }

    private void updateMicroserviceSystem(Microservice microservice) {
      microserviceSystem.getMicroservices().removeIf(microservice1 -> microservice1.getName().equals(microservice.getName()));
      microserviceSystem.getMicroservices().add(microservice);
    }

//
//  private void updateApiDestinationsAdd(JClass service, String servicePath) {
//    for (RestCall restCall : service.getRestCalls()) {
//      for (Microservice ms : msModelMap.values()) {
//        if (!ms.getId().equals(servicePath)) {
//          for (JClass controller : ms.getControllers()) {
//            // Reassign controller if it is in the deltas
//            String classPath = controller.getClassPath();
//            JClass deltaController =
//                systemChange.getControllers().values().stream()
//                    .filter(delta -> delta.getChangedClass().getClassPath().equals(classPath))
//                    .map(delta -> (JClass) delta.getChangedClass())
//                    .findFirst()
//                    .orElse(null);
//
//            if (Objects.nonNull(deltaController)) {
//              controller = deltaController;
//            }
//
//            for (Endpoint endpoint : controller.getEndpoints()) {
//              if (endpoint.matchCall(restCall)) {
//                restCall.setDestination(controller);
//                endpoint.addCall(restCall, service);
//              }
//            }
//          }
//        }
//      }
//    }
//  }
//
//  private void updateApiDestinationsDelete(JClass controller, String servicePath) {
//    for (Endpoint endpoint : controller.getEndpoints()) {
//      for (Microservice ms : msModelMap.values()) {
//        if (!ms.getId().equals(servicePath)) {
//          for (JClass service : ms.getServices()) {
//            for (RestCall restCall : service.getRestCalls()) {
//              if (endpoint.matchCall(restCall)
//                  && !restCall.pointsToDeletedFile()
//                  && !"".equals(restCall.getDestFile())) {
//                restCall.setDestinationAsDeleted();
//              }
//            }
//          }
//        }
//      }
//    }
//  }
 }
