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

    /**
     * Method to get a specific value from the contents of the annotation.
     * Assumes contents are in the format "[key = \"value\"]".
     *
     * @param key the key to look up in the contents
     * @return the value corresponding to the key, or null if not found
     */
    public String getValue(String key) {
        if (contents == null || contents.isEmpty()) {
            return null;
        }

        // Remove surrounding brackets and split into key-value pairs
        String cleanedContents = contents.substring(1, contents.length() - 1).trim();
        String[] pairs = cleanedContents.split(",");

        // Search for the key in each pair
        for (String pair : pairs) {
            pair = pair.trim();
            String[] keyValue = pair.split("=");

            if (keyValue.length == 2 && keyValue[0].trim().equals(key)) {
                return keyValue[1].trim().replace("\"", "");
            }
        }

        return null; // Key not found
    }
}
