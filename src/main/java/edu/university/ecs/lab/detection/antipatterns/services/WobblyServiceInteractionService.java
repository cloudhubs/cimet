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

/**
 * Service for detecting wobbly service interactions within a microservice system based on specific annotations.
 */
public class WobblyServiceInteractionService {

    /**
     * Detects all wobbly/unstable service interactions in the given network graph
     * 
     * @param currentSystem The microservice system representing microservices and their dependencies.
     * @return A WobblyServiceInteraction object containing a list of services with wobbly interactions.
     */
    public WobblyServiceInteraction findWobblyServiceInteractions(MicroserviceSystem currentSystem) {
        List<String> wobblyInteractions = new ArrayList<>();

        for (Microservice microservice : currentSystem.getMicroservices()) {
            Set<JClass> classes = microservice.getClasses();

            for (JClass jClass : classes) {
                boolean hasCircuitBreaker = false;
                boolean hasRateLimiter = false;
                boolean hasRetry = false;
                boolean hasBulkhead = false;
                boolean passes = false;

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
                    
                    // Add each method with CircuitBreaker, RateLimiter, Retry, and Bulkhead annotations to 
                    // wobblyInteractions list
                    if (hasCircuitBreaker && hasRateLimiter && hasRetry && hasBulkhead) {
                        String interaction = microservice.getName() + "." + jClass.getName() + "." + method.getName();
                        wobblyInteractions.add(interaction);
                        
                        hasCircuitBreaker= false;
                        hasRateLimiter = false;
                        hasRetry = false;
                        hasBulkhead = false;
                    }
                }
                
            }
        }

        // Create and return a WobblyServiceInteraction object with the aggregated list
        return new WobblyServiceInteraction(wobblyInteractions);
    }
}
