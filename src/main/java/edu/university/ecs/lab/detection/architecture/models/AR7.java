package edu.university.ecs.lab.detection.architecture.models;

import java.util.ArrayList;
import java.util.List;

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

        JClass oldClass = oldSystem.findClass(delta.getOldPath());

        // Return empty list if it isn't modify or not a repository
        if (!delta.getChangeType().equals(ChangeType.MODIFY) || !oldClass.getClassRole().equals(ClassRole.REPOSITORY)) {
            return useCases;
        }

        for (Method methodOld : oldClass.getMethods()) {
            for (Method methodNew : delta.getClassChange().getMethods()) {
                // Match old and new Methods
                if (methodOld.getID().equals(methodNew.getID()) && !methodOld.equals(methodNew)) {
                    // Any annotations that don't equal their counterpart we can add to metadata
                    for (Annotation annotationOld : methodOld.getAnnotations()) {
                        for (Annotation annotationNew : methodNew.getAnnotations()) {
                            // Annotation names match but not the contents
                            if (annotationNew.getName().equals(annotationOld.getName()) && !annotationNew.getContents().equals(annotationOld.getContents())) {
                                AR7 useCase7 = new AR7();
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.isJsonNull();
                                jsonObject.addProperty("OldMethodDeclaration", methodOld.getID());
                                jsonObject.addProperty("NewMethodDeclaration", methodNew.getID());
                                useCase7.setOldCommitID(oldSystem.getCommitID());
                                useCase7.setNewCommitID(newSystem.getCommitID());

                                useCase7.setMetaData(jsonObject);
                                useCases.add(useCase7);
                            }
                        }
                    }
                }
            }
        }

        return useCases;
    }
}
