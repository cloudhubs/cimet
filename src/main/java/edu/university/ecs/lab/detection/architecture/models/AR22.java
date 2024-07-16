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
public class AR22 extends AbstractAR {
    protected static final String TYPE = "Architectural Rule 22";
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

    public static List<AR22> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {

        // If it isn't add or modify, or not a service
        if (delta.getChangeType().equals(ChangeType.MODIFY) || !delta.getClassChange().getClassRole().equals(ClassRole.ENTITY)) {
            return new ArrayList<>();
        }


        AR22 archRules22 = new AR22();
        JsonObject jsonObject = new JsonObject();
        archRules22.setMetaData(jsonObject);
        archRules22.setOldCommitID(oldSystem.getCommitID());
        archRules22.setNewCommitID(newSystem.getCommitID());

        return List.of(archRules22);

    }

}