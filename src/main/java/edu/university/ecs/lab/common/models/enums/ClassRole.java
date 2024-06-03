package edu.university.ecs.lab.common.models.enums;

import edu.university.ecs.lab.common.models.JClass;
import lombok.Getter;

import java.io.File;

/** Enum to represent the role of a class in a system */
public enum ClassRole {
  CONTROLLER(JClass.class),
  SERVICE(JClass.class),
  REPOSITORY(JClass.class),
  ENTITY(JClass.class),
  DTO(JClass.class),
  UNKNOWN(null),
  /** Only for metrics reporting, not a real class role */
  TOTAL(null);

  /** Get the associated class type for a role */
  @Getter private final Class<? extends JClass> classType;

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

  /**
   * Get the class role from the source file path
   *
   * @param sourceFile the source file to parse the path of
   * @return the class role from the source file
   */
  public static ClassRole fromSourceFile(File sourceFile) {
    String fileName = sourceFile.getName().toLowerCase();
    String parentPath = sourceFile.getParent().toLowerCase();

    if (fileName.contains("controller")) {
      return ClassRole.CONTROLLER;
    } else if (fileName.contains("service")) {
      return ClassRole.SERVICE;
    } else if (fileName.contains("dto")) {
      return ClassRole.DTO;
    } else if (fileName.contains("repository")) {
      return ClassRole.REPOSITORY;
    } else if (parentPath.contains("entity") || parentPath.contains("model")) {
      return ClassRole.ENTITY;
    } else {
      return ClassRole.UNKNOWN;
    }
  }
}
