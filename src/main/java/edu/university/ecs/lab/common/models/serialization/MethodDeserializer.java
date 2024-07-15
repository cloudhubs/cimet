package edu.university.ecs.lab.common.models.serialization;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.ir.Annotation;
import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.Field;
import edu.university.ecs.lab.common.models.ir.Method;
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

        List<Annotation> annotations = new ArrayList<Annotation>();
        for (JsonElement annotationJson : json.get("annotations").getAsJsonArray()) {
            annotations.add(context.deserialize(annotationJson, Annotation.class));
        }
        method.setAnnotations(annotations);

        Set<Field> fields = new HashSet<Field>();
        for (JsonElement fieldJson : json.get("parameters").getAsJsonArray()) {
            fields.add(context.deserialize(fieldJson, Field.class));
        }
        method.setParameters(fields);
        method.setPackageAndClassName(json.get("packageAndClassName").getAsString());


        return method;
    }

    private Method jsonToEndpoint(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        Method method = jsonToMethod(json, context);
        String microserviceName = json.get("microserviceName").getAsString();
        String url = json.get("url").getAsString();
        String httpMethod = json.get("httpMethod").getAsString();


        return new Endpoint(method, url, HttpMethod.valueOf(httpMethod), microserviceName);
    }

}
