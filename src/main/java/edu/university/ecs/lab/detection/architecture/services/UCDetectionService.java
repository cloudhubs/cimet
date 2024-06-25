package edu.university.ecs.lab.detection.architecture.services;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.detection.architecture.models.UseCase;
import edu.university.ecs.lab.detection.architecture.models.UseCase2;
import edu.university.ecs.lab.detection.architecture.models.UseCase3;
import edu.university.ecs.lab.detection.architecture.models.UseCase4;
import edu.university.ecs.lab.detection.architecture.models.UseCase7;

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
            List<UseCase2> useCase2List = UseCase2.scan(d, microserviceSystemOld.findClass(d.getOldPath()), microserviceSystemNew);
            if (!useCase2List.isEmpty()){
                useCases.addAll(useCase2List);
            }
            
            List<UseCase3> useCase3List = UseCase3.scan(d, microserviceSystemNew);
            if (!useCase3List.isEmpty()){
                useCases.addAll(useCase3List);
            }

            List<UseCase4> useCase4List = UseCase4.scan(d, microserviceSystemOld, microserviceSystemNew);
            if(!useCase4List.isEmpty()){
                useCases.addAll(useCase4List);
            }

            //Need to check implementation
            List<UseCase7> useCase7List = UseCase7.scan(d, microserviceSystemOld.findClass(d.getNewPath()));
            if(!useCase7List.isEmpty()){
                useCases.addAll(useCase7List);
            }
        }

        return useCases;
    }
}
