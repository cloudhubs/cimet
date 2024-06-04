//package edu.university.ecs.lab.common.models;
//
//import com.google.gson.annotations.SerializedName;
//import edu.university.ecs.lab.common.models.enums.HttpMethod;
//import lombok.Getter;
//
//import javax.json.Json;
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import java.util.Objects;
//
///**
// * Represents an extension of a method call. A rest call exists at the service level and represents
// * a call to an endpoint mapping.
// */
//@Getter
//public class RestCall extends MethodCall {
//  /** String constant for a deleted file TODO not yet implemented */
//  public static final String DEST_DELETED = "DELETED";
//
//  /** The api url that is targeted in rest call */
//  @SerializedName("dest-endpoint")
//  private String destEndpoint;
//
//  @SerializedName("dest-msId")
//  private String destMsId;
//
//  @SerializedName("dest-file")
//  private String destFile;
//
//  /**
//   * The httpMethod of the api endpoint e.g. GET, POST, PUT see semantics.models.enums.httpMethod
//   */
//  private String httpMethod;
//
//  /** The object holding payload for api call */
//  private String payloadObject;
//
//  /**
//   * Constructor for RestCall
//   *
//   * @param methodName Name of the method
//   * @param objectName Name of the object
//   * @param calledFrom Name of the method that contains this call
//   * @param msId Name of the service that contains this method
//   * @param httpMethod The http method of the call
//   * @param destEndpoint The endpoint of the call
//   * @param destMsId The destination service of the call
//   * @param destFile The destination file of the call
//   * @param payloadObject Object holding payload for api call
//   */
//  public RestCall(
//      String methodName,
//      String objectName,
//      String calledFrom,
//      String msId,
//      HttpMethod httpMethod,
//      String destEndpoint,
//      String destMsId,
//      String destFile,
//      String payloadObject) {
//    super(methodName, objectName, calledFrom, msId);
//    this.httpMethod = httpMethod.name();
//    this.destEndpoint = destEndpoint;
//    this.destMsId = destMsId;
//    this.destFile = destFile;
//    this.payloadObject = payloadObject;
//  }
//
//  public JsonObject toJsonObject() {
//    // Get "restCall" methodCalls in service
//    JsonObjectBuilder restCallBuilder = super.createBuilder();
//
//    restCallBuilder.add("httpMethod", httpMethod);
//    restCallBuilder.add("dest-endpoint", destEndpoint);
//    restCallBuilder.add("dest-msId", destMsId);
//    restCallBuilder.add("dest-file", destFile);
//    restCallBuilder.add("payloadObject", payloadObject);
//
//    return restCallBuilder.build();
//  }
//
//  public void setDestination(JController destController) {
//    this.destMsId = destController.getMsId();
//    setDestFile(destController.getClassPath());
//  }
//
//  /**
//   * Set and sanitize the destination file. Private because {@link #setDestination(JController)}
//   * should be used instead
//   */
//  private void setDestFile(String destFile) {
//    this.destFile = destFile.replaceAll("\\\\", "/");
//  }
//
//  /** Set the destination of this call to a deleted file */
//  public void setDestinationAsDeleted() {
//    setDestFile(DEST_DELETED);
//    this.destMsId = DEST_DELETED;
//  }
//
//  /**
//   * Check if the destination of this call is a deleted file
//   *
//   * @return True if the destination is a deleted file, false otherwise
//   */
//  public boolean pointsToDeletedFile() {
//    return DEST_DELETED.equals(destFile);
//  }
//
//  /**
//   * Get a string representation of this call
//   *
//   * @return The string representation
//   */
//  public String getId() {
//    return msId + "#" + calledFrom + "[" + httpMethod + "]" + "->" + destMsId + ":" + destEndpoint;
//  }
//
//  /** Represents a call as an endpoint source. */
//  @Getter
//  public static class EndpointCall implements JsonSerializable {
//    @SerializedName("src-msId")
//    private String msId;
//
//    @SerializedName("src-id")
//    private String id;
//
//    @SerializedName("src-file")
//    private String srcFile;
//
//    /**
//     * Convert constructor to EndpointCall
//     *
//     * @param call RestCall object
//     */
//    public EndpointCall(RestCall call, JService service) {
//      this.msId = call.getMsId();
//      this.id = call.getId();
//      this.srcFile = service.getClassPath();
//    }
//
//    /**
//     * Check if the given call matches this call.
//     *
//     * @param call The call to compare with
//     * @return True if the calls match, false otherwise
//     */
//    public boolean matches(RestCall call) {
//      return this.msId.equals(call.getMsId()) && this.id.equals(call.getId());
//    }
//
//    /**
//     * @return Converted JsonObject of RestCall object
//     */
//    @Override
//    public JsonObject toJsonObject() {
//      // Get "restCall" methodCalls in service
//      JsonObjectBuilder restCallBuilder = Json.createObjectBuilder();
//
//      restCallBuilder.add("src-msId", msId);
//      restCallBuilder.add("src-id", id);
//      restCallBuilder.add("src-file", srcFile);
//
//      return restCallBuilder.build();
//    }
//  }
//
//  public static boolean hasDestination(String msDestination) {
//    return !Objects.equals(msDestination, "")
//        && !Objects.equals(msDestination, RestCall.DEST_DELETED);
//  }
//}
