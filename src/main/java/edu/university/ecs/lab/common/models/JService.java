//package edu.university.ecs.lab.common.models;
//
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.Setter;
//
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import java.util.ArrayList;
//import java.util.List;
//
//import static edu.university.ecs.lab.common.utils.ObjectToJsonUtils.listToJsonArray;
//
//@Getter
//@Setter
//public class JService extends JClass implements JsonSerializable {
//  /** List of rest calls in the service */
//  private List<RestCall> restCalls;
//
//  /**
//   * Convert constructor for a JService object. Initializes restCalls to an empty list
//   *
//   * @param jClass the JClass object to convert
//   */
//  public JService(@NonNull JClass jClass) {
//    super(
//        jClass.getClassName(),
//        jClass.getClassPath(),
//        jClass.getPackageName(),
//        jClass.getClassRole(),
//        jClass.getMethods(),
//        jClass.getFields(),
//        jClass.getAnnotations(),
//        jClass.getMethodCalls(),
//        jClass.getMsId());
//    this.restCalls = new ArrayList<>();
//  }
//
//  @Override
//  public JsonObject toJsonObject() {
//    JsonObjectBuilder builder = super.createBuilder();
//
//    builder.add("restCalls", listToJsonArray(restCalls));
//
//    return builder.build();
//  }
//}
