package edu.university.ecs.lab.common.models;

import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

/**
 * Abstract class for general datatypes that fall under
 * JClass
 */
@Data
public abstract class Node implements JsonSerializable {
    /**
     * Name of the structure
     */
    protected String name;

    /**
     * Name of the package + class (package path e.g. edu.university.lab.AdminController)
     */
    protected String packageAndClassName;

    /**
     * This method generates a unique ID for datatypes that fall
     * under a JClass
     *
     * @return the string unique ID
     */
    public final String getID() {
        return packageAndClassName + name;
    }
}
