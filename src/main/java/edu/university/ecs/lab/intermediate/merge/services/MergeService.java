 package edu.university.ecs.lab.intermediate.merge.services;

 import edu.university.ecs.lab.common.config.Config;
 import edu.university.ecs.lab.common.config.ConfigUtil;
 import edu.university.ecs.lab.common.models.*;
 import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
 import edu.university.ecs.lab.delta.models.Delta;
 import edu.university.ecs.lab.delta.models.SystemChange;

 import java.nio.file.Path;
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

            String path = d.getOldPath() == null ? d.getNewPath() : d.getOldPath();

            // Check for pom.xml
            if(!path.endsWith(".java")) {
                continue;
            }

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
      // Here the path is irrelevant since it does not change
        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(getMicroserviceNameFromPath(delta.getOldPath()))).findFirst().orElse(null);

        // If we dont find a microservice
        if(Objects.isNull(ms)) {
            // Check the orphan pool
            for(JClass orphan : microserviceSystem.getOrphans()) {
                // If found remove it and return
                if(orphan.getClassPath().equals(delta.getOldPath())) {
                    microserviceSystem.getOrphans().remove(orphan);

                    // Only add it back if we parsed a valid JClass (not null)
                    if(delta.getClassChange() != null) {
                        microserviceSystem.getOrphans().add(delta.getClassChange());
                    }

                    return;
                }
            }
            return;
        }

        List<JClass> classes = ms.getClasses();

        for(JClass jClass : classes) {
            if(jClass.getClassPath().equals(delta.getOldPath())) {
                ms.removeClass(delta.getOldPath());

                // Only add it back if we parsed a valid JClass (not null)
                if(delta.getClassChange() != null) {
                    ms.addJClass(delta.getClassChange());
                }

                return;
            }
        }

        // If we modify a class that was previously invalid
        // and we dont find it in previous classes or orphans
        // we should still add it because it might have been invalid
        // when we first tried to add it and was dropped
        if(delta.getClassChange() != null) {
            ms.addJClass(delta.getClassChange());
        }

    }

  public void addFile(Delta delta) {


      Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(getMicroserviceNameFromPath(delta.getNewPath()))).findFirst().orElse(null);

      // If we cant find his microservice after we called updateMicroservices then a file was pushed without a pom.xml
      // so it will be held as an orphan
      if(Objects.isNull(ms)) {
          microserviceSystem.getOrphans().add(delta.getClassChange());
          return;
      }

      ms.addJClass(delta.getClassChange());


  }

    public void removeFile(Delta delta) {
        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(getMicroserviceNameFromPath(delta.getOldPath()))).findFirst().orElse(null);

        // If we are removing a file and it's microservice doesn't exist
        if(Objects.isNull(ms)) {
            // Check the orphan pool
            for(JClass orphan : microserviceSystem.getOrphans()) {
                // If found remove it and return
                if(orphan.getClassPath().equals(delta.getOldPath())) {
                    microserviceSystem.getOrphans().remove(orphan);
                    return;
                }
            }
            return;
        }

        ms.removeClass(delta.getOldPath());


    }

    // Check if the last JClass was deleted
//    private void checkDeleteMicroservices(Microservice microservice) {
//      if(microservice.getServices().isEmpty() && microservice.getRepositories().isEmpty() && microservice.getControllers().isEmpty()) {
//          microserviceSystem.getMicroservices().removeIf(microservice1 -> microservice1.getName().equals(microservice.getName()));
//      }
//    }

    // Check for the creation / deletion of microservices depending on actions done to pom.xml/dockerfile
    private void updateMicroservices(List<Delta> deltaChanges) {

      List<Delta> pomDeltas = deltaChanges.stream().filter(delta -> (delta.getOldPath() == null ? delta.getNewPath() : delta.getOldPath()).endsWith("pom.xml")).collect(Collectors.toUnmodifiableList());
      // Loop through changes to pom.xml files
      for(Delta delta : pomDeltas) {
          Microservice microservice;
          String[] tokens;

          String path = delta.getOldPath() == null ? delta.getNewPath() : delta.getOldPath();
          tokens = path.split("\\\\");

          // Skip a pom that is in the root
          if(tokens.length <= 4) {
              continue;
          }

          switch (delta.getChangeType()) {
              case ADD:
                  microservice = new Microservice(tokens[tokens.length - 2], delta.getNewPath().replace("\\pom.xml", ""));
                  // Here we must check if any orphans are waiting on this creation
                  microserviceSystem.adopt(microservice);
                  microserviceSystem.getMicroservices().add(microservice);
                  break;
              case DELETE:
                  microservice = microserviceSystem.findMicroserviceByName(getMicroserviceNameFromPath(delta.getOldPath()));
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

     private String getMicroserviceNameFromPath(String path) {
        for(Microservice microservice : microserviceSystem.getMicroservices()) {
            if(path.contains(microservice.getPath())) {
                return microservice.getName();
            }
        }

        return null;
     }
 }
