//package edu.university.ecs.lab.delta.models;
//
//import com.google.gson.*;
//import com.google.gson.annotations.SerializedName;
//import edu.university.ecs.lab.common.models.JClass;
//import edu.university.ecs.lab.common.models.JsonSerializable;
//import edu.university.ecs.lab.common.models.enums.ClassRole;
//import edu.university.ecs.lab.delta.models.enums.ChangeType;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.Setter;
//import org.eclipse.jgit.diff.DiffEntry;
//
//import javax.json.Json;
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import java.io.Reader;
//import java.lang.reflect.Type;
//
//@Getter
//@Setter
//@AllArgsConstructor
//public class Delta implements JsonSerializable, JsonDeserializer<Delta> {
//  /** JSON key for the changes field */
//  public static final String CHANGES = "changes";
//
//  /**
//   * Relative path to the changed file. This DIFFERS from {@link JClass#getClassPath()} as the
//   * jClass path starts at the repoName and this is a working relative path to the file.
//   */
//  private String localPath;
//
//  /** The type of change that occurred */
//  private ChangeType changeType;
//
//  /** The commit id that the delta was generated from */
//  private String commitId;
//
//  /** The microservice id of the changed class */
//  private String msId;
//
//  /** The class that was changed */
//  @SerializedName(CHANGES)
//  private JClass changedClass;
//
//  /**
//   * @return the microservice id of the changed class
//   */
//  public String getMsId() {
//    return changedClass.getMsId();
//  }
//
//  /**
//   * @param msId the microservice id of the changed class
//   */
//  public void setMsId(String msId) {
//    changedClass.setMsId(msId);
//    this.msId = msId;
//  }
//
//  /**
//   * @param entry diff entry from git
//   * @param jClass class extracted from the CHANGED file
//   * @param localPath relative path to the service (ex:
//   *     ./clonePath/repoName/service/path/to/file.java)
//   */
//  public Delta(JClass jClass, DiffEntry entry, String localPath) {
//    // TODO Validate local path
//    setLocalPath(localPath);
//    setChangeType(ChangeType.fromDiffEntry(entry));
//    setCommitId(entry.getNewId().name());
//    setChangedClass(jClass);
//    setMsId(jClass.getMsId());
//  }
//
//  /**
//   * Converts the delta object to a JSON object
//   *
//   * @return the JSON object representation of the delta
//   */
//  public JsonObject toJsonObject() {
//    JsonObjectBuilder jout = Json.createObjectBuilder();
//
//    jout.add("changeType", changeType.name());
//    jout.add("commitId", commitId);
//    jout.add("classRole", changedClass.getClassRole().name());
//    jout.add("msId", msId);
//    jout.add("localPath", localPath);
//    jout.add(CHANGES, changedClass.toJsonObject());
//    return jout.build();
//  }
//
//  /** Private constructor only for {@link #getAdapter()}. */
//  private Delta() {}
//
//  /**
//   * Get the adapter for the {@link GsonBuilder#registerTypeAdapter(Type, Object)} interface.
//   *
//   * @return a blank delta object
//   */
//  public static Object getAdapter() {
//    return new Delta();
//  }
//
//  /**
//   * Deserialize a JSON object from {@link Gson#fromJson(Reader, Type)} into a Delta object
//   *
//   * @param jsonElement the JSON element to deserialize
//   * @param type the type of the object to deserialize
//   * @param jsonDeserializationContext the context of the deserialization
//   * @return the deserialized Delta object
//   * @throws JsonParseException if the JSON element cannot be deserialized
//   */
//  @Override
//  public Delta deserialize(
//      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
//      throws JsonParseException {
//    com.google.gson.JsonObject deltaObj = jsonElement.getAsJsonObject();
//    String deltaType = deltaObj.get("classRole").getAsString();
//
//    Class<? extends JClass> classType = ClassRole.classFromRoleName(deltaType);
//    JClass changedClass = new Gson().fromJson(deltaObj.get(CHANGES), classType);
//
//    return new Delta(
//        deltaObj.get("localPath").getAsString(),
//        ChangeType.valueOf(deltaObj.get("changeType").getAsString()),
//        deltaObj.get("commitId").getAsString(),
//        deltaObj.get("msId").getAsString(),
//        changedClass);
//  }
//}
