package edu.university.ecs.lab.detection.architecture.services;

import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.architecture.models.UseCase;

import java.util.List;

public class UCDetectionService {
    MicroserviceSystem microserviceSystemOld;
    MicroserviceSystem microserviceSystemNew;

    public UCDetectionService(String oldIRPath, String newIRPath) {
        microserviceSystemOld = JsonReadWriteUtils.readFromJSON(oldIRPath, MicroserviceSystem.class);
        microserviceSystemNew = JsonReadWriteUtils.readFromJSON(newIRPath, MicroserviceSystem.class);
    }



    public double calculateAverage(List<UseCase> oldUseCases, List<UseCase> newUseCases) {
        double deltaChange = 0;
        double oldScore = 0;
        double newScore = 0;

        for (UseCase oldUseCase : oldUseCases) {
            oldScore += oldUseCase.getWeight() * 1;
        }

        for (UseCase newUseCase : newUseCases) {
            newScore += newUseCase.getWeight() * 1;
        }

        return oldScore / newScore;
    }
}
