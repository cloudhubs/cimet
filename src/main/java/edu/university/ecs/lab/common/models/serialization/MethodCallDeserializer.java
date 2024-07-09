package edu.university.ecs.lab.common.models.serialization;

import com.google.gson.*;
import edu.university.ecs.lab.common.models.ir.MethodCall;
import edu.university.ecs.lab.common.models.ir.RestCall;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import java.lang.reflect.Type;

/**
 * Class for deserializing a MethodCall when using Gson
 */
public class MethodCallDeserializer implements JsonDeserializer<MethodCall> {

    @Override
    public MethodCall deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("url")) {
            return jsonToRestCall(jsonObject, context);
        } else {
            return jsonToMethodCall(jsonObject, context);
        }
    }

    private MethodCall jsonToMethodCall(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        MethodCall methodCall = new MethodCall();
        methodCall.setName(json.get("name").getAsString());
        methodCall.setCalledFrom(json.get("calledFrom").getAsString());
        methodCall.setObjectName(json.get("objectName").getAsString());
        methodCall.setParameterContents(json.get("parameterContents").getAsString());
        methodCall.setPackageAndClassName(json.get("packageAndClassName").getAsString());
        methodCall.setObjectType(json.get("objectType").getAsString());


        return methodCall;
    }

    private RestCall jsonToRestCall(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        MethodCall methodCall = jsonToMethodCall(json, context);
        String microserviceName = json.get("microserviceName").getAsString();
        String url = json.get("url").getAsString();
        String httpMethod = json.get("httpMethod").getAsString();


        return new RestCall(methodCall, url, HttpMethod.valueOf(httpMethod), microserviceName);
    }
}
