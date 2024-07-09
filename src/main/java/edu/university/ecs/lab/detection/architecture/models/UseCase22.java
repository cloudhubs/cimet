package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import edu.university.ecs.lab.detection.architecture.models.enums.Confidence;
import lombok.Data;
import java.util.*;

@Data
public class UseCase22 extends AbstractUseCase {
    protected static final String TYPE = "UseCase22";
    protected static final String NAME = "Entity Modification";
    protected static final String DESC = "Any entity has been modified inconsistently among services";
    protected static final Confidence CONFIDENCE = Confidence.UNKNOWN;
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
    public String getType() {
        return TYPE;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    public static List<UseCase22> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {

        // If it isn't add or modify, or not a service
        if (delta.getChangeType().equals(ChangeType.MODIFY) || !delta.getClassChange().getClassRole().equals(ClassRole.ENTITY)) {
            return new ArrayList<>();
        }


        UseCase22 useCase22 = new UseCase22();
        JsonObject jsonObject = new JsonObject();
        useCase22.setMetaData(jsonObject);
        useCase22.setOldCommitID(oldSystem.getCommitID());
        useCase22.setNewCommitID(newSystem.getCommitID());

        return List.of(useCase22);

    }

}
