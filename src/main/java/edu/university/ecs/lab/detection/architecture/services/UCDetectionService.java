package edu.university.ecs.lab.detection.architecture.services;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.detection.architecture.models.*;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class UCDetectionService {
    SystemChange oldSystem;
    MicroserviceSystem microserviceSystemOld;
    MicroserviceSystem microserviceSystemNew;

    public UCDetectionService(String DeltaPath, String OldIRPath, String NewIRPath) {
        oldSystem = JsonReadWriteUtils.readFromJSON(DeltaPath, SystemChange.class);
        microserviceSystemOld = JsonReadWriteUtils.readFromJSON(OldIRPath, MicroserviceSystem.class);
        microserviceSystemNew = JsonReadWriteUtils.readFromJSON(NewIRPath, MicroserviceSystem.class);
    }

    public List<AbstractAR> scanUseCases() {

        List<AbstractAR> useCases = new ArrayList<>();
        useCases.addAll(scanDeltaUC());
        useCases.addAll(scanSystemUC());

        return useCases;
    }



    public List<AbstractAR> scanDeltaUC() {
        List<AbstractAR> useCases = new ArrayList<>();
        
        for (Delta d : oldSystem.getChanges()){
            
            if((d.getNewPath() != null) && d.getNewPath().contains("pom.xml") || (d.getOldPath() != null && d.getOldPath().contains("pom.xml"))){
                continue;
            }
            // List<UseCase1> useCase1List = UseCase1.scan(d, microserviceSystemOld, microserviceSystemNew);
            // if (!useCase1List.isEmpty()){
            //     useCases.addAll(useCase1List);
            // }


            List<AR6> useCase6List = AR6.scan(d, microserviceSystemOld, microserviceSystemNew);
            if(!useCase6List.isEmpty()){
                useCases.addAll(useCase6List);
            }

            
            // List<UseCase7> useCase7List = UseCase7.scan(d, microserviceSystemOld, microserviceSystemNew);
            // if(!useCase7List.isEmpty()){
            //     useCases.addAll(useCase7List);
            // }
        }

        return useCases;
    }

    public List<AbstractAR> scanSystemUC() {
        List<AbstractAR> useCases = new ArrayList<>();

        List<AR3> useCase3List = AR3.scan2(microserviceSystemOld, microserviceSystemNew);
        if (!useCase3List.isEmpty()){
            useCases.addAll(useCase3List);
        }

        List<AR4> useCase4List = AR4.scan2(microserviceSystemOld, microserviceSystemNew);
        if(!useCase4List.isEmpty()){
            useCases.addAll(useCase4List);
        }

        List<AR20> useCase20List = AR20.scan(microserviceSystemOld, microserviceSystemNew);
        if(!useCase20List.isEmpty()){
            useCases.addAll(useCase20List);
        }

        // List<UseCase21> useCase21List = UseCase21.scan(microserviceSystemOld, microserviceSystemNew);
        // if(!useCase21List.isEmpty()){
        //     useCases.addAll(useCase21List);
        // }

        return useCases;
    }
}
