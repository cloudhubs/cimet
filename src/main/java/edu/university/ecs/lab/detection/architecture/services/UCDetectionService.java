package edu.university.ecs.lab.detection.architecture.services;

import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.Microservice;
import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.common.models.RestCall;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import edu.university.ecs.lab.detection.architecture.models.UseCase;
import edu.university.ecs.lab.detection.architecture.models.UseCase2;
import edu.university.ecs.lab.detection.architecture.models.UseCase3;
import edu.university.ecs.lab.detection.architecture.models.UseCase4;
import edu.university.ecs.lab.detection.architecture.models.UseCase5;

import java.util.ArrayList;
import java.util.List;

public class UCDetectionService {
    SystemChange oldSystem;
    MicroserviceSystem microserviceSystemOld;
    MicroserviceSystem microserviceSystemNew;

    public UCDetectionService(String DeltaPath, String OldIRPath, String IRPath) {
        oldSystem = JsonReadWriteUtils.readFromJSON(DeltaPath, SystemChange.class);
        microserviceSystemOld = JsonReadWriteUtils.readFromJSON(OldIRPath, MicroserviceSystem.class);
        microserviceSystemNew = JsonReadWriteUtils.readFromJSON(IRPath, MicroserviceSystem.class);
    }



    public List<UseCase> scanDelta() {
        List<UseCase> useCases = new ArrayList<>();
        
        for (Delta d : oldSystem.getChanges()){
            if (d.getChangeType().equals(ChangeType.DELETE) && d.getClassChange().getClassRole().equals(ClassRole.CONTROLLER)){
                JClass oldClass = microserviceSystemOld.findClass(d.getOldPath());
                for (Endpoint endpoint: oldClass.getEndpoints()){
                    UseCase2 useCase2 = UseCase2.scan(endpoint, microserviceSystemNew);
                    if (useCase2 != null){
                        useCases.add(useCase2);
                    }
                }
            }
            
            if (d.getChangeType().equals(ChangeType.ADD) && d.getClassChange().getClassRole().equals(ClassRole.SERVICE)){
                for (RestCall rc: d.getClassChange().getRestCalls()){
                    UseCase3 useCase3 = UseCase3.scan(rc, microserviceSystemNew);
                    if (useCase3 != null){
                        useCases.add(useCase3);
                    }
                }
            }

            if((d.getChangeType().equals(ChangeType.MODIFY) || d.getChangeType().equals(ChangeType.DELETE)) && d.getClassChange().getClassRole().equals(ClassRole.SERVICE)){
                if(d.getChangeType().equals(ChangeType.MODIFY)){
                    List<RestCall> modifiedRestCalls = new ArrayList<>();
                    JClass oldClass = microserviceSystemOld.findClass(d.getNewPath());
                    for(RestCall restCallNew: d.getClassChange().getRestCalls()){
                        outer: {
                            for(RestCall restCallOld: oldClass.getRestCalls()){
                                if(restCallOld.equals(restCallNew)){
                                    break outer;
                                }
                            }
                            modifiedRestCalls.add(restCallNew);
                        }
                    }
                    for (RestCall rc: modifiedRestCalls){
                        UseCase4 useCase4 = UseCase4.scan(rc, microserviceSystemNew);
                        if (useCase4 != null){
                            useCases.add(useCase4);
                        }
                    }
                }
                else if(d.getChangeType().equals(ChangeType.DELETE)){
                    JClass oldClass = microserviceSystemOld.findClass(d.getOldPath());
                    for (RestCall rc: oldClass.getRestCalls()){
                        UseCase4 useCase4 = UseCase4.scan(rc, microserviceSystemNew);
                        if (useCase4 != null){
                            useCases.add(useCase4);
                        }
                    }
                }
            }

            if ((d.getChangeType().equals(ChangeType.ADD) || d.getChangeType().equals(ChangeType.DELETE)) && d.getClassChange().getClassRole().equals(ClassRole.SERVICE)){
                if(d.getChangeType().equals(ChangeType.DELETE)){
                    JClass oldPath = microserviceSystemOld.findClass(d.getOldPath());
                    for (RestCall rc: oldPath.getRestCalls()){
                        UseCase5 useCase5 = UseCase5.scan(rc, microserviceSystemNew);
                        if (useCase5 != null){
                            useCases.add(useCase5);
                        }
                    }
                }
                else{
                    for (RestCall rc: d.getClassChange().getRestCalls()){
                        UseCase5 useCase5 = UseCase5.scan(rc, microserviceSystemNew);
                        if (useCase5 != null){
                            useCases.add(useCase5);
                        }
                    }
                }
            }
        }

        return useCases;
    }
}
