//package edu.university.ecs.lab.common.models;
//
//import com.google.gson.annotations.SerializedName;
//import lombok.*;
//
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//import static edu.university.ecs.lab.common.utils.ObjectToJsonUtils.listToJsonArray;
//
///**
// * Represents an extension of a method declaration. An endpoint exists at the controller level and
// * signifies an open mapping that can be the target of a rest call.
// */
//@ToString
//@Setter
//@Getter
//public class Endpoint extends Method implements JsonSerializable {
//  /** The URL of the endpoint e.g. /api/v1/users/login, May have parameters like {param} */
//  @SerializedName("api")
//  private String url;
//
//  /** The type of endpoint, e.g. GetMapping, PostMapping, etc. */
//  @SerializedName("type")
//  private String decorator;
//
//  /** The HTTP method of the endpoint, e.g. GET, POST, etc. */
//  private String httpMethod;
//
//  /** The microservice id that this endpoint belongs to */
//  private String msId;
//
//  /** The calls that use this endpoint */
//  @SerializedName("src-calls")
//  private List<RestCall.EndpointCall> srcCalls;
//
//  /**
//   * Constructor for an endpoint.
//   *
//   * @param method the method that this endpoint represents
//   * @param url the URL of the endpoint
//   * @param decorator the type of endpoint
//   * @param httpMethod the HTTP method of the endpoint
//   * @param msId the microservice id that this endpoint belongs to
//   */
//  public Endpoint(Method method, String url, String decorator, String httpMethod, String msId) {
//    super(
//        method.getMethodName(),
//        method.getParameterList(),
//        method.getReturnType(),
//        method.getAnnotations());
//    setMsId(msId);
//    setUrl(url);
//    setDecorator(decorator);
//    setHttpMethod(httpMethod);
//    setSrcCalls(new ArrayList<>());
//  }
//
//  @Override
//  public JsonObject toJsonObject() {
//    JsonObjectBuilder endpointBuilder = super.createBuilder();
//
//    endpointBuilder.add("id", getId());
//    endpointBuilder.add("api", url);
//    endpointBuilder.add("type", decorator);
//    endpointBuilder.add("httpMethod", httpMethod);
//    endpointBuilder.add("msId", msId);
//    endpointBuilder.add("src-calls", listToJsonArray(srcCalls));
//
//    return endpointBuilder.build();
//  }
//
//  /**
//   * Constructs a String endpointId from an Endpoint object and name of microservice.
//   *
//   * @return a unique Id representing this endpoint
//   */
//  public String getId() {
//    return "[" + httpMethod + "]" + msId + ":" + url;
//  }
//
//  /**
//   * Check if the given RestCall matches this endpoint. Does not use restCall destMsId or destFile
//   * as these may not be set yet.
//   *
//   * @param restCall the call to check
//   * @return true if the call matches this endpoint, false otherwise
//   */
//  public boolean matchCall(RestCall restCall) {
//
//    if (!this.httpMethod.equals(restCall.getHttpMethod())) {
//      return false;
//    }
//
//    boolean isUrlMatch = this.url.equals(restCall.getDestEndpoint());
//
//    // TODO TESTING, this is a hack to handle endpoints with params
//    if (!isUrlMatch) {
//      String urlWithoutParams = this.url.split("/\\{")[0];
//      if (!Objects.equals(urlWithoutParams, this.url)) {
//        isUrlMatch = urlWithoutParams.equals(restCall.getDestEndpoint());
//      }
//    }
//
//    return isUrlMatch;
//  }
//
//  /**
//   * Compare this endpoint to another (changed) endpoint to determine if they are the same.
//   *
//   * @param other the endpoint to compare to
//   * @return true if the endpoints are the same, false otherwise
//   */
//  public boolean isSameEndpoint(Endpoint other) {
//    return Objects.equals(httpMethod, other.getHttpMethod()) && Objects.equals(url, other.getUrl());
//  }
//
//  /**
//   * Add a call to the list of calls that use this endpoint.
//   *
//   * @param restCall the call to add
//   * @param service service containing the rest call
//   */
//  public void addCall(RestCall restCall, JService service) {
//    srcCalls.add(new RestCall.EndpointCall(restCall, service));
//  }
//}
