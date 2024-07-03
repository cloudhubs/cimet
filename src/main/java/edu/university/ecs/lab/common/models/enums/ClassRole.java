package edu.university.ecs.lab.common.models.enums;

import edu.university.ecs.lab.common.models.ir.JClass;
import lombok.Getter;

/**
 * Enum to represent the role of a class in a system
 */
public enum ClassRole {
    CONTROLLER(JClass.class),
    SERVICE(JClass.class),
    REPOSITORY(JClass.class),
    ENTITY(JClass.class),
    EMBEDDABLE(JClass.class),
    UNKNOWN(null);

    /**
     * Get the associated class type for a role
     */
    @Getter
    private final Class<? extends JClass> classType;

    /**
     * Private constructor to link enum to class type
     *
     * @param classType the class type to associate with the role
     */
    ClassRole(Class<? extends JClass> classType) {
        this.classType = classType;
    }

    /**
     * Get the class role from the class type
     *
     * @param roleName the name of the class role
     * @return associated class type if it exists, else null (unknown or not found)
     */
    public static Class<? extends JClass> classFromRoleName(String roleName) {
        // Iterate over type names
        for (ClassRole role : ClassRole.values()) {
            if (role.name().equalsIgnoreCase(roleName)) {
                return role.classType;
            }
        }
        return null;
    }

}
