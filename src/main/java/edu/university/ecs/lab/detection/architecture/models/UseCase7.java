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
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase7 extends AbstractUseCase {
    protected static final String NAME = "Affected endpoint due to data access logic update";
    protected static final Scope SCOPE = Scope.METHOD;
    protected static final String DESC = "A repository method was modified and now causes inconsistent results";
    protected JsonObject metaData;

    private UseCase7() {}

    @Override
    public List<? extends AbstractUseCase> checkUseCase() {
        // This method should return the list of UseCase3 instances relevant to UseCase7 logic, if any.
        ArrayList<UseCase3> useCases = new ArrayList<>();
        return useCases;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public Scope getScope() {
        return SCOPE;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    public static List<UseCase7> scan(Delta delta, MicroserviceSystem oldSystem) {
        List<UseCase7> useCases = new ArrayList<>();

        JClass oldClass = oldSystem.findClass(delta.getOldPath());

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
                                UseCase7 useCase7 = new UseCase7();
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.isJsonNull();
                                jsonObject.add("OldMethodDeclaration", methodOld.toJsonObject());
                                jsonObject.add("NewMethodDeclaration", methodNew.toJsonObject());
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
