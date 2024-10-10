package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.Annotation;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Method;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.Data;

/**
 * Architectural Rule 7 Class: Affected endpoint due to data access logic update
 */
@Data
public class AR7 extends AbstractAR {
    protected static final String TYPE = "Architectural Rule 7";
    protected static final String NAME = "Affected endpoint due to data access logic update";
    protected static final String DESC = "A repository method was modified and now causes inconsistent results";
    private String oldCommitID;
    private String newCommitID;
    protected JsonObject metaData;


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Scan and compare old microservice system and new microservice system to identify endpoints affected by data access logic update
     * 
     * @param delta change between old commit and new microservice systems
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list of methods with modified annotations
     */
    public static List<AR7> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<AR7> useCases = new ArrayList<>();

        JClass jClass = oldSystem.findClass(delta.getOldPath());
        if(jClass == null) {
            return new ArrayList<>();
        }

        // Return empty list if it isn't modify or not a repository
        if (!delta.getChangeType().equals(ChangeType.MODIFY) || !jClass.getClassRole().equals(ClassRole.REPOSITORY)) {
            return useCases;
        }


        for (Method methodOld : jClass.getMethods()) {
            outer:
            {
                for (Method methodNew : delta.getClassChange().getMethods()) {
                    // Match old and new Methods
                    if (methodOld.getID().equals(methodNew.getID())) {
                        // Flag any added, removed
                        Set<String> oldAnnotations = methodOld.getAnnotations().stream()
                                .map(Annotation::getName)
                                .collect(Collectors.toSet());
                        Set<String> newAnnotations = methodNew.getAnnotations().stream()
                                .map(Annotation::getName)
                                .collect(Collectors.toSet());

                        // Check for added or removed annotations
                        Set<String> addedAnnotations = new HashSet<>(newAnnotations);
                        addedAnnotations.removeAll(oldAnnotations);

                        Set<String> removedAnnotations = new HashSet<>(oldAnnotations);
                        removedAnnotations.removeAll(newAnnotations);

                        if (!addedAnnotations.isEmpty() || !removedAnnotations.isEmpty()) {
                            AR7 useCase7 = new AR7();
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("OldMethodDeclaration", methodOld.getID());
                            jsonObject.addProperty("NewMethodDeclaration", methodNew.getID());
                            jsonObject.addProperty("AddedAnnotations", addedAnnotations.toString());
                            jsonObject.addProperty("RemovedAnnotations", removedAnnotations.toString());
                            useCase7.setOldCommitID(oldSystem.getCommitID());
                            useCase7.setNewCommitID(newSystem.getCommitID());
                            useCase7.setMetaData(jsonObject);
                            useCases.add(useCase7);
                            break outer;
                        }

                        // Check for modified annotations (same name but different contents)
                        for (Annotation annotationOld : methodOld.getAnnotations()) {
                            for (Annotation annotationNew : methodNew.getAnnotations()) {
                                if (annotationNew.getName().equals(annotationOld.getName())
                                        && !annotationNew.getContents().equals(annotationOld.getContents())) {
                                    AR7 useCase7 = new AR7();
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.addProperty("OldMethodDeclaration", methodOld.getID());
                                    jsonObject.addProperty("NewMethodDeclaration", methodNew.getID());
                                    jsonObject.addProperty("ModifiedAnnotation", annotationNew.getName());
                                    useCase7.setOldCommitID(oldSystem.getCommitID());
                                    useCase7.setNewCommitID(newSystem.getCommitID());
                                    useCase7.setMetaData(jsonObject);
                                    useCases.add(useCase7);
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return useCases;
    }
}
