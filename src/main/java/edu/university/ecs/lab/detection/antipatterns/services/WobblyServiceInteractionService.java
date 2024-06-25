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
     * Checks for wobbly service interactions in the given MicroserviceSystem and returns the results.
     *
     * @param system the MicroserviceSystem to analyze
     * @return a list of WobblyServiceInteraction objects representing detected wobbly interactions
     */
    public List<WobblyServiceInteraction> checkForWobblyServiceInteractions(MicroserviceSystem system) {
        List<WobblyServiceInteraction> wobblyInteractions = new ArrayList<>();
        for (Microservice microservice : system.getMicroservices()) {
            checkClassesForWobblyInteractions(microservice.getClasses(), microservice, wobblyInteractions);
        }

        return wobblyInteractions;
    }

    /**
     * Helper method to check a set of JClass objects for wobbly service interactions.
     *
     * @param classes            the set of JClass objects to analyze
     * @param microservice       the Microservice owning the classes
     * @param wobblyInteractions list to store detected wobbly interactions
     */
    private void checkClassesForWobblyInteractions(Set<JClass> classes, Microservice microservice, List<WobblyServiceInteraction> wobblyInteractions) {
        for (JClass jClass : classes) {
            for (Annotation annotation : jClass.getAnnotations()) {
                if (isWobblyServiceInteractionAnnotation(annotation)) {
                    String microserviceName = microservice.getName();
                    String className = jClass.getName();
                    String methodName = getMethodNameFromAnnotation(annotation);
                    WobblyServiceInteraction interaction = new WobblyServiceInteraction(microserviceName, className, methodName);
                    wobblyInteractions.add(interaction);
                }
            }
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
