package edu.university.ecs.lab.detection.architecture.services;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.detection.architecture.models.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for detecting architectural rule violations
 */
public class ARDetectionService {
    SystemChange oldSystem;
    MicroserviceSystem microserviceSystemOld;
    MicroserviceSystem microserviceSystemNew;

    /**
     * Construct from paths to JSON files
     * 
     * @param DeltaPath path to delta JSON file
     * @param OldIRPath path to old commit JSON file
     * @param NewIRPath path to new commit JSON file
     */
    public ARDetectionService(String DeltaPath, String OldIRPath, String NewIRPath) {
        oldSystem = JsonReadWriteUtils.readFromJSON(DeltaPath, SystemChange.class);
        microserviceSystemOld = JsonReadWriteUtils.readFromJSON(OldIRPath, MicroserviceSystem.class);
        microserviceSystemNew = JsonReadWriteUtils.readFromJSON(NewIRPath, MicroserviceSystem.class);
    }

    /**
     * Construct with System objects
     * 
     * @param oldSystem SystemChange object representing the delta between commits
     * @param microserviceSystemOld old microservice commit object
     * @param microserviceSystemNew new microservice commit object
     */
    public ARDetectionService(SystemChange oldSystem, MicroserviceSystem microserviceSystemOld, MicroserviceSystem microserviceSystemNew) {
        this.oldSystem = oldSystem;
        this.microserviceSystemOld = microserviceSystemOld;
        this.microserviceSystemNew = microserviceSystemNew;
    }

    /**
     * Scans all use cases for architectural rule violations
     * 
     * @return list of architectural rule violations
     */
    public List<AbstractAR> scanUseCases() {

        List<AbstractAR> useCases = new ArrayList<>();
        useCases.addAll(scanDeltaUC());
        useCases.addAll(scanSystemUC());

        return useCases;
    }

    /**
     * Scans delta use cases for architectural rule violations
     * 
     * @return list of delta use cases that violate architectural rules
     */
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

            
             List<AR7> useCase7List = AR7.scan(d, microserviceSystemOld, microserviceSystemNew);
             if(!useCase7List.isEmpty()){
                 useCases.addAll(useCase7List);
             }
        }

        return useCases;
    }

    /**
     * Scans ALL use cases in new system commit for architectural rule violations
     * 
     * @return list of architectural rule violations from new system commit
     */
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

//        List<AR7> useCase7List = AR7.scan(demicroserviceSystemOld, microserviceSystemNew);
//        if(!useCase7List.isEmpty()){
//            useCases.addAll(useCase7List);
//        }

        // List<UseCase21> useCase21List = UseCase21.scan(microserviceSystemOld, microserviceSystemNew);
        // if(!useCase21List.isEmpty()){
        //     useCases.addAll(useCase21List);
        // }

        return useCases;
    }
}
