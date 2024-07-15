package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.ir.Annotation;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Method;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.detection.antipatterns.models.WobblyServiceInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WobblyServiceInteractionService {

    public WobblyServiceInteraction findWobblyServiceInteractions(MicroserviceSystem currentSystem) {
        List<String> wobblyInteractions = new ArrayList<>();

        for (Microservice microservice : currentSystem.getMicroservices()) {
            String methodName = "";
            Set<JClass> classes = microservice.getClasses();

            for (JClass jClass : classes) {
                boolean hasCircuitBreaker = false;
                boolean hasRateLimiter = false;
                boolean hasRetry = false;
                boolean hasBulkhead = false;

                // Check annotations at the method level
                for (Method method : jClass.getMethods()) {
                    for (Annotation annotation : method.getAnnotations()) {
                        if (annotation.getName().equals("CircuitBreaker")) {
                            hasCircuitBreaker = true;
                        } else if (annotation.getName().equals("RateLimiter")) {
                            hasRateLimiter = true;
                        } else if (annotation.getName().equals("Retry")) {
                            hasRetry = true;
                        } else if (annotation.getName().equals("Bulkhead")) {
                            hasBulkhead = true;
                        }
                    }
                    methodName = method.getName();
                }

                // If all required annotations are present, consider it a wobbly service interaction
                if (hasCircuitBreaker && hasRateLimiter && hasRetry && hasBulkhead) {
                    String interaction = microservice.getName() + "." + jClass.getName() + "." + methodName;
                    wobblyInteractions.add(interaction);
                }
            }
        }

        return new WobblyServiceInteraction(wobblyInteractions);
    }
}
