package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.detection.antipatterns.models.WrongCuts;

import java.util.*;

/**
 * Service class for identifying and reporting clusters of wrongly interconnected services (Wrong Cuts)
 * within a microservice network graph.
 */
public class WrongCutsService {
    /**
     * Detects all clusters of wrongly interconnected services in the given network graph.
     *
     * @param currentSystem The microservice system representing microservices and their dependencies.
     * @return A WrongCuts object containing a list of services that are wrongly interconnected.
     */
    public WrongCuts detectWrongCuts(MicroserviceSystem currentSystem) {
        List<String> wrongCutServices = new ArrayList<>();

        for (Microservice microservice : currentSystem.getMicroservices()){
            if (microservice.getRepositories().isEmpty()){
                wrongCutServices.add(microservice.getName());
            }
        }

        // Create and return a WrongCuts object with the aggregated list
        return new WrongCuts(wrongCutServices);
    }
}
