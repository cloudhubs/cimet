 package edu.university.ecs.lab.intermediate.merge.services;

 import edu.university.ecs.lab.common.config.Config;
 import edu.university.ecs.lab.common.config.ConfigUtil;
 import edu.university.ecs.lab.common.error.Error;
 import edu.university.ecs.lab.common.models.*;
 import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
 import edu.university.ecs.lab.delta.models.Delta;
 import edu.university.ecs.lab.delta.models.SystemChange;
 import edu.university.ecs.lab.delta.models.enums.ChangeType;
 import edu.university.ecs.lab.delta.models.enums.FileType;

 import java.io.File;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 import java.util.stream.Collectors;


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

  public void generateMergeIR() {
      System.out.println("Merging to new IR!");

      // TODO optimize
      // If no changes are present we will write back out same IR
      if(Objects.isNull(systemChange.getChanges())) {
          JsonReadWriteUtils.writeToJSON("./output/IR.json", microserviceSystem);
          return;
      }

      // First we make necessary changes to microservices
      updateMicroservices(systemChange.getChanges());

        for(Delta d : systemChange.getChanges()) {
            switch (d.getChangeType()) {
                case ADD:
                    addFile(d);
                    break;
                case MODIFY:
                    modifyFiles(d);
                    break;
                case DELETE:
                    removeFile(d);
                    break;
            }
        }

        JsonReadWriteUtils.writeToJSON("./output/IR.json", microserviceSystem);
  }


    public void modifyFiles(Delta delta) {
         removeFile(delta);
         addFile(delta);
    }

  public void addFile(Delta delta) {
      if(Objects.isNull(delta.getClassChange())) {
          return;
      }

      Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(delta.getNewMicroserviceName())).findFirst().orElse(null);


      switch(delta.getClassChange().getClassRole()) {
          case CONTROLLER:
               ms.getControllers().add(delta.getClassChange());
               break;
          case SERVICE:
              ms.getServices().add(delta.getClassChange());
              break;
          case REPOSITORY:
              ms.getRepositories().add(delta.getClassChange());
              break;

      }

  }

    public void removeFile(Delta delta) {
        if(Objects.isNull(delta.getClassChange())) {
            return;
        }

        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(delta.getOldMicroserviceName())).findFirst().orElse(null);

        if(Objects.isNull(ms)) {
            System.out.println(delta.toJsonObject());
            Error.reportAndExit(Error.NULL_ERROR);
        }

        JClass jClass = microserviceSystem.getClassByPath(delta.getNewPath());

        switch(jClass.getClassRole()) {
            case CONTROLLER:
                ms.getControllers().remove(jClass);
                break;
            case SERVICE:
                ms.getServices().remove(jClass);
                break;
            case REPOSITORY:
                ms.getRepositories().remove(jClass);
                break;

        }

//        checkDeleteMicroservices(ms);

    }

    // Check if the last JClass was deleted
//    private void checkDeleteMicroservices(Microservice microservice) {
//      if(microservice.getServices().isEmpty() && microservice.getRepositories().isEmpty() && microservice.getControllers().isEmpty()) {
//          microserviceSystem.getMicroservices().removeIf(microservice1 -> microservice1.getName().equals(microservice.getName()));
//      }
//    }

    // Check for the creation / deletion of microservices depending on actions done to pom.xml/dockerfile
    private void updateMicroservices(List<Delta> deltaChanges) {

      // Loop through changes to pom.xml files
      for(Delta delta : deltaChanges.stream().filter(delta -> delta.getOldPath().endsWith("pom.xml") || delta.getNewPath().endsWith("pom.xml")).collect(Collectors.toUnmodifiableList())) {

          Microservice microservice;
          switch (delta.getChangeType()) {
              case ADD:
                  microservice = new Microservice(delta.getNewMicroserviceName(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                  // Here we must check if any orphans are waiting on this creation
                  microserviceSystem.adopt(microservice, delta.getNewMicroserviceName());
                  microserviceSystem.getMicroservices().add(microservice);
                  break;

              case MODIFY:
                  microservice = microserviceSystem.findMicroserviceByName(delta.getOldMicroserviceName());
                  microserviceSystem.getMicroservices().remove(microservice);
                  microservice.setName(delta.getNewMicroserviceName());
                  // Here we must check if any orphans are waiting on this creation
                  microserviceSystem.adopt(microservice, delta.getNewPath());
                  microserviceSystem.getMicroservices().add(microservice);
                  break;

              case DELETE:
                  microservice = microserviceSystem.findMicroserviceByName(delta.getOldMicroserviceName());
                  // Here we must orphan all the classes of this microservice
                  microserviceSystem.orphanize(microservice);
                  microserviceSystem.getMicroservices().remove(microservice);
                  break;

          }

      }

    }

    // If the parent of this filePath is the p
    private boolean searchDeltas(String filePath) {

      for(Delta delta : systemChange.getChanges()) {
          if(delta.getNewPath().equals(filePath)) {
              return true;
          }
      }

      return false;

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
