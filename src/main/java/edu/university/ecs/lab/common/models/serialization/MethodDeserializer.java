package edu.university.ecs.lab.common.models.serialization;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * Class for deserializing a Method when using Gson
 */
public class MethodDeserializer implements JsonDeserializer<Method> {

    @Override
    public Method deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("url")) {
            return jsonToEndpoint(jsonObject, context);
        } else {
            return jsonToMethod(jsonObject, context);
        }
    }

    private Method jsonToMethod(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        Method method = new Method();
        method.setName(json.get("name").getAsString());
        method.setReturnType(json.get("returnType").getAsString());

        Set<Annotation> annotations = new HashSet<>();
        for (JsonElement annotationJson : json.get("annotations").getAsJsonArray()) {
            annotations.add(context.deserialize(annotationJson, Annotation.class));
        }
        method.setAnnotations(annotations);

        Set<Parameter> parameters = new HashSet<>();
        for (JsonElement fieldJson : json.get("parameters").getAsJsonArray()) {
            parameters.add(context.deserialize(fieldJson, Parameter.class));
        }
        method.setParameters(parameters);
        method.setPackageAndClassName(json.get("packageAndClassName").getAsString());
        method.setMicroserviceName(json.get("microserviceName").getAsString());
        method.setClassName(json.get("className").getAsString());


        return method;
    }

    private Method jsonToEndpoint(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        Method method = jsonToMethod(json, context);
        String url = json.get("url").getAsString();
        String httpMethod = json.get("httpMethod").getAsString();


        return new Endpoint(method, url, HttpMethod.valueOf(httpMethod));
    }

}
