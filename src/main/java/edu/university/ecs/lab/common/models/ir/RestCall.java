package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Represents an extension of a method call. A rest call exists at the service level and represents
 * a call to an endpoint mapping.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class RestCall extends MethodCall {

    /**
     * The URL of the rest call e.g. /api/v1/users/login, May have dynamic parameters
     * which are converted to {?}
     */
    private String url;

    /**
     * The httpMethod of the api endpoint e.g. GET, POST, PUT see semantics.models.enums.httpMethod
     */
    private HttpMethod httpMethod;


    public RestCall(String methodName, String packageAndClassName, String objectType, String objectName, String calledFrom, String parameterContents, String microserviceName) {
        super(methodName, packageAndClassName, objectType, objectName, calledFrom, parameterContents, microserviceName);
    }

    public RestCall(MethodCall methodCall, String url, HttpMethod httpMethod) {
        super(methodCall.name, methodCall.packageAndClassName, methodCall.objectType, methodCall.objectName, methodCall.calledFrom, methodCall.parameterContents, methodCall.microserviceName);
        this.url = url;
        this.httpMethod = httpMethod;
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();

        jsonObject.addProperty("url", url);
        jsonObject.addProperty("httpMethod", httpMethod.name());

        return jsonObject;
    }

    public static boolean matchEndpoint(RestCall restcall, Endpoint endpoint){
        if(restcall.getMicroserviceName().equals(endpoint.getMicroserviceName())){
            return false;
        }

        return restcall.getUrl().equals(endpoint.getUrl()) && restcall.getHttpMethod().equals(endpoint.getHttpMethod());
    }
}
