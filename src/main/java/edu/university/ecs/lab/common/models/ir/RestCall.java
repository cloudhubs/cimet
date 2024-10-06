package edu.university.ecs.lab.common.models.ir;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;


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




    public RestCall(String methodName, String packageAndClassName, String objectType, String objectName, String calledFrom, String parameterContents,
                    String microserviceName, String className) {
        super(methodName, packageAndClassName, objectType, objectName, calledFrom, parameterContents,
                microserviceName, className);
    }

    public RestCall(MethodCall methodCall, String url, HttpMethod httpMethod) {
        super(methodCall.name, methodCall.packageAndClassName, methodCall.objectType, methodCall.objectName, methodCall.calledFrom, methodCall.parameterContents,
                methodCall.microserviceName, methodCall.className);
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

    /**
     * Checks if a rest call matches a given endpoint
     * 
     * @param restcall rest call to match
     * @param endpoint endpoint to match
     * @return true if rest call and enpoint match, false otherwise
     */
    public static boolean matchEndpoint(RestCall restcall, Endpoint endpoint){
        if(restcall.getMicroserviceName().equals(endpoint.getMicroserviceName())){
            return false;
        }

        int queryParamIndex = restcall.getUrl().replace("{?}", "temp").indexOf("?");
        String baseURL = queryParamIndex == -1 ? restcall.getUrl() : restcall.getUrl().substring(0, queryParamIndex);
        return baseURL.equals(endpoint.getUrl()) && (restcall.getHttpMethod().equals(endpoint.getHttpMethod()) || endpoint.getHttpMethod().equals(HttpMethod.ALL)) && matchQueryParams(restcall, endpoint, queryParamIndex);
    }

    /**
     * Checks if rest call parameters match parameters for the target endpoint
     * 
     * @param restCall rest call to match
     * @param endpoint endpoint to match
     * @param queryParamIndex string index at which query parameters start
     * @return true if parameters match, false otherwise
     */
    private static boolean matchQueryParams(RestCall restCall, Endpoint endpoint, int queryParamIndex) {
        for(Parameter parameter : endpoint.getParameters()) {
            for(Annotation annotation : parameter.getAnnotations()) {
                if(annotation.getName().equals("RequestParam")) {
                    String queryParameterName = "";
                    if(annotation.getAttributes().containsKey("default")) {
                        queryParameterName = annotation.getAttributes().get("default");
                    } else if(annotation.getAttributes().containsKey("name")) {
                        if(annotation.getAttributes().containsKey("required")
                                && annotation.getAttributes().get("required").equals("false")) {
                            continue;
                        }
                        queryParameterName = annotation.getAttributes().get("name");
                    } else {
                        queryParameterName = parameter.getName();
                    }

                    if(!restCall.getUrl().substring(queryParamIndex + 1, restCall.getUrl().length()).contains(queryParameterName + "=")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
