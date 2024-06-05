package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

import java.util.List;

/**
 * Represents a class in Java. It holds all information regarding that class including all method
 * declarations, method calls, fields, etc.
 */
@Data
@AllArgsConstructor
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
  private List<Method> methods;

  /** List of class variables e.g. (private String username;) */
  private List<Field> fields;

  /** Class level annotations * */
  private List<Annotation> annotations;

  /** List of method invocations made from within this class e.g. obj.method() */
  private List<MethodCall> methodCalls;


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

    jsonObject.addProperty("className", getClassName());
    jsonObject.addProperty("classPath", getClassPath());
    jsonObject.addProperty("packageName", getPackageName());
    jsonObject.addProperty("classRole", getClassRole().name());
    jsonObject.add("methods", JsonSerializable.toJsonArray(getMethods()));
    jsonObject.add("variables", JsonSerializable.toJsonArray(getFields()));
    jsonObject.add("methodCalls", JsonSerializable.toJsonArray(getMethodCalls()));
    jsonObject.add("annotations", JsonSerializable.toJsonArray(getAnnotations()));

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
