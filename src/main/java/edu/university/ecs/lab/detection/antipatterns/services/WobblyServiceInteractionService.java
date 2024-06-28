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
 * Service class for detecting wobbly service interactions in a MicroserviceSystem.
 * Wobbly service interactions are identified based on specific annotations in classes and methods.
 */
public class WobblyServiceInteractionService {

    /**
     * Finds all wobbly service interactions in the given network graph.
     * 
     * @param graph the network graph to analyze
     * @return a WobblyServiceInteraction object containing the list of detected interactions
     */
    public WobblyServiceInteraction findWobblyServiceInteractions(MicroserviceSystem currentSystem) {
        List<String> wobblyInteractions = new ArrayList<>();

        for (Microservice microservice : currentSystem.getMicroservices()) {
            Set<JClass> classes = microservice.getClasses();
            for (JClass jClass : classes) {
                for (Annotation annotation : jClass.getAnnotations()) {
                    if (isWobblyServiceInteractionAnnotation(annotation)) {
                        String interaction = microservice + "." + jClass.getName() + "." + getMethodNameFromAnnotation(annotation);
                        wobblyInteractions.add(interaction);
                    }
                }
                for (Method method : jClass.getMethods()) {
                    for (Annotation annotation : method.getAnnotations()) {
                        if (isWobblyServiceInteractionAnnotation(annotation)) {
                            String interaction = microservice + "." + jClass.getName() + "." + method.getName();
                            wobblyInteractions.add(interaction);
                        }
                    }
                }
            }
        }

        return new WobblyServiceInteraction(wobblyInteractions);
    }

    /**
     * Checks if an annotation represents a wobbly service interaction.
     *
     * @param annotation the annotation to check
     * @return true if the annotation indicates a wobbly service interaction, false otherwise
     */
    private boolean isWobblyServiceInteractionAnnotation(Annotation annotation) {
        String annotationName = annotation.getName();
        return annotationName.equals("CircuitBreaker") ||
                annotationName.equals("RateLimiter") ||
                annotationName.equals("Retry") ||
                annotationName.equals("Bulkhead");
    }

    /**
     * Extracts the method name from a wobbly service interaction annotation.
     *
     * @param annotation the annotation containing the method name
     * @return the method name extracted from the annotation
     */
    private String getMethodNameFromAnnotation(Annotation annotation) {
        return annotation.getValue("fallbackMethod");
    }
}
