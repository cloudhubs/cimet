package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import lombok.*;

import java.util.List;
import java.util.Objects;

/**
 * Represents an extension of a method declaration. An endpoint exists at the controller level and
 * signifies an open mapping that can be the target of a rest call.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Endpoint extends Method {

    /**
     * The URL of the endpoint e.g. /api/v1/users/login, May have parameters like {param}
     * which are converted to {?}
     */
    private String url;

    /**
     * The HTTP method of the endpoint, e.g. GET, POST, etc.
     */
    private HttpMethod httpMethod;

    /**
     * The microservice id that this endpoint belongs to
     */
    private String microserviceName;

    public Endpoint(String methodName, List<Field> parameters, String returnType, List<Annotation> annotations) {
      super(methodName, parameters, returnType, annotations);
    }
    public Endpoint(Method method, String url, HttpMethod httpMethod, String microserviceName) {
      super(method.getMethodName(), method.getParameters(), method.getReturnType(), method.getAnnotations());
      this.url = url;
      this.httpMethod = httpMethod;
      this.microserviceName = microserviceName;
    }

  /**
     * The calls that use this endpoint
     */
//  private List<RestCall.EndpointCall> srcCalls;
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();

        jsonObject.addProperty("url", url);
        jsonObject.addProperty("httpMethod", httpMethod.name());
        jsonObject.addProperty("microserviceName", microserviceName);

        return jsonObject;
    }

//    /**
//     * Constructs a String endpointId from an Endpoint object and name of microservice.
//     *
//     * @return a unique Id representing this endpoint
//     */
//    public String getId() {
//        return "[" + httpMethod + "]" + msId + ":" + url;
//    }

//    /**
//     * Check if the given RestCall matches this endpoint. Does not use restCall destMsId or destFile
//     * as these may not be set yet.
//     *
//     * @param restCall the call to check
//     * @return true if the call matches this endpoint, false otherwise
//     */
//    public boolean matchCall(RestCall restCall) {
//
//        if (!this.httpMethod.equals(restCall.getHttpMethod())) {
//            return false;
//        }
//
//        boolean isUrlMatch = this.url.equals(restCall.getDestEndpoint());
//
//        // TODO TESTING, this is a hack to handle endpoints with params
//        if (!isUrlMatch) {
//            String urlWithoutParams = this.url.split("/\\{")[0];
//            if (!Objects.equals(urlWithoutParams, this.url)) {
//                isUrlMatch = urlWithoutParams.equals(restCall.getDestEndpoint());
//            }
//        }
//
//        return isUrlMatch;
//    }

    /**
     * Compare this endpoint to another (changed) endpoint to determine if they are the same.
     *
     * @param other the endpoint to compare to
     * @return true if the endpoints are the same, false otherwise
     */
    public boolean isSameEndpoint(Endpoint other) {
        return Objects.equals(httpMethod, other.getHttpMethod()) && Objects.equals(url, other.getUrl());
    }

//    /**
//     * Add a call to the list of calls that use this endpoint.
//     *
//     * @param restCall the call to add
//     * @param service  service containing the rest call
//     */
//    public void addCall(RestCall restCall, JService service) {
//        srcCalls.add(new RestCall.EndpointCall(restCall, service));
//    }
}
