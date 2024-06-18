package edu.university.ecs.lab.detection.antipatterns.services;

import edu.university.ecs.lab.common.models.Annotation;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.Method;
import edu.university.ecs.lab.common.models.Microservice;
import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.detection.antipatterns.models.WobblyServiceInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WobblyServiceInteractionService {

    // List to hold the results
    private List<WobblyServiceInteraction> wobblyInteractions = new ArrayList<>();

    // Method to check for wobbly service interactions
    public void checkForWobblyServiceInteractions(MicroserviceSystem system) {
        // Iterate over each microservice in the system
        for (Microservice microservice : system.getMicroservices()) {
            // Check all classes in the microservice for wobbly interactions
            checkClassesForWobblyInteractions(microservice.getClasses(), microservice);
        }
    }

    // Helper method to check a set of JClass objects for wobbly service interactions
    private void checkClassesForWobblyInteractions(Set<JClass> classes, Microservice microservice) {
        for (JClass jClass : classes) {
            // Check annotations of each class
            for (Annotation annotation : jClass.getAnnotations()) {
                if (isWobblyServiceInteractionAnnotation(annotation)) {
                    String microserviceName = microservice.getName();
                    String className = jClass.getName();
                    String methodName = getMethodNameFromAnnotation(annotation);
                    WobblyServiceInteraction interaction = new WobblyServiceInteraction(microserviceName, className, methodName);
                    wobblyInteractions.add(interaction);
                }
            }
            // Check methods of each class
            for (Method method : jClass.getMethods()) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (isWobblyServiceInteractionAnnotation(annotation)) {
                        String microserviceName = microservice.getName();
                        String className = jClass.getName();
                        String methodName = method.getName();
                        WobblyServiceInteraction interaction = new WobblyServiceInteraction(microserviceName, className, methodName);
                        wobblyInteractions.add(interaction);
                    }
                }
            }
        }
    }

    // Method to check if an annotation represents a wobbly service interaction
    private boolean isWobblyServiceInteractionAnnotation(Annotation annotation) {
        String annotationName = annotation.getName();
        return annotationName.equals("CircuitBreaker") ||
                annotationName.equals("RateLimiter") ||
                annotationName.equals("Retry") ||
                annotationName.equals("Bulkhead");
    }

    // Method to extract the method name from a wobbly service interaction annotation
    private String getMethodNameFromAnnotation(Annotation annotation) {
        return annotation.getValue("fallbackMethod");
    }
}
