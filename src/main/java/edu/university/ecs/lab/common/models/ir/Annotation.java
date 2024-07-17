package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents an annotation in Java
 */
@Data
@EqualsAndHashCode
public class Annotation extends Node {
    /**
     * The contents of the annotation *
     */
    protected String contents;

    public Annotation(String name, String packageAndClassName, String contents) {
        this.name = name;
        this.packageAndClassName = packageAndClassName;
        this.contents = contents;
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("packageAndClassName", getPackageAndClassName());
        jsonObject.addProperty("contents", getContents());

        return jsonObject;
    }
}
