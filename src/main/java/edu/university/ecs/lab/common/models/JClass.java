package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

import java.util.List;
import java.util.Set;

/**
 * Represents a class in Java. It holds all information regarding that class including all method
 * declarations, method calls, fields, etc.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class JClass implements JsonSerializable {
  /** Name of the class e.g. Food */
  private String className;

  /** Path like repoName/.../serviceName/.../file.java */
  private String classPath;

  /** Full java package name of the class e.g. com.cloudhubs.trainticket.food.entity */
  private String packageName;

  /**
   * Role of the class in the microservice system. See {@link ClassRole} for possibilities. Will
   * match with subtype where applicable
   */
  private ClassRole classRole;

  /** List of methods in the class */
  private Set<Method> methods;

  /** List of class variables e.g. (private String username;) */
  private Set<Field> fields;

  /** Class level annotations * */
  private Set<Annotation> annotations;

  /** List of method invocations made from within this class e.g. obj.method() */
  private Set<MethodCall> methodCalls;


  public void setClassName(String className) {
    this.className = className.replace(".java", "");
  }

  /** Uniquely identify a class as an object of a given service */
//  public String getId() {
//    return classRole.name() + ":" + msId + "#" + className;
//  }

  /**
   * Set the class path of the class. This will replace all "\\" with "/" for readability.
   *
   * @param classPath The class path to set
   */
  public void setClassPath(String classPath) {
    this.classPath = classPath.replaceAll("\\\\", "/");
  }

  /**
   * Convert a single JClass to a JsonObject
   *
   * @return Converted JsonObject of JClass object
   */
  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("packageName", getPackageName());
    jsonObject.addProperty("className", getClassName());
    jsonObject.addProperty("classPath", getClassPath());
    jsonObject.addProperty("classRole", getClassRole().name());
    jsonObject.add("annotations", JsonSerializable.toJsonArray(getAnnotations()));
    jsonObject.add("fields", JsonSerializable.toJsonArray(getFields()));
    jsonObject.add("methods", JsonSerializable.toJsonArray(getMethods()));
    jsonObject.add("methodCalls", JsonSerializable.toJsonArray(getMethodCalls()));

    return jsonObject;
  }

  /**
   * Check if the given class is the same as this class. This is true if they have the same
   * classPath.
   *
   * @param other The class to compare with
   * @return True if the classes are the same, false otherwise
   */
  public boolean matchClassPath(JClass other) {
    return this.getClassPath().equals(other.getClassPath());
  }
}
