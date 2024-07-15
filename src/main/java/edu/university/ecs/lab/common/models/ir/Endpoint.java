package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.List;

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

    public Endpoint(String methodName, String packageName, Set<Field> parameters, String returnType, List<Annotation> annotations) {
        super(methodName, packageName, parameters, returnType, annotations);
    }

    public Endpoint(Method method, String url, HttpMethod httpMethod, String microserviceName) {
        super(method.getName(), method.getPackageAndClassName(), method.getParameters(), method.getReturnType(), method.getAnnotations());
        this.url = url;
        this.httpMethod = httpMethod;
        this.microserviceName = microserviceName;
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();

        jsonObject.addProperty("url", url);
        jsonObject.addProperty("httpMethod", httpMethod.name());
        jsonObject.addProperty("microserviceName", microserviceName);

        return jsonObject;
    }

}
