package edu.university.ecs.lab.common.models;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.*;

/** Represents a flow from controller level down to DAO. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Flow implements JsonSerializable {
    private Microservice model;
    private JClass controller;
    private Endpoint controllerMethod;
    private MethodCall serviceMethodCall;
    private Field controllerServiceField;
    private JClass service;
    private Method serviceMethod;
    private MethodCall repositoryMethodCall;
    private Field serviceRepositoryField;
    private JClass repository;
    private Method repositoryMethod;

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("model", model == null ? JsonNull.INSTANCE : model.toJsonObject());
        jsonObject.add("controller", controller == null ? JsonNull.INSTANCE : controller.toJsonObject());
        jsonObject.add("controllerMethod", controllerMethod == null ? JsonNull.INSTANCE : controllerMethod.toJsonObject());
        jsonObject.add("serviceMethodCall", serviceMethodCall == null ? JsonNull.INSTANCE : serviceMethodCall.toJsonObject());
        jsonObject.add("service", service == null ? JsonNull.INSTANCE : service.toJsonObject());
        jsonObject.add("serviceMethod", serviceMethod == null ? JsonNull.INSTANCE : serviceMethod.toJsonObject());
        jsonObject.add("repositoryMethodCall", repositoryMethodCall == null ? JsonNull.INSTANCE : repositoryMethodCall.toJsonObject());
        jsonObject.add("serviceRepositoryField", serviceRepositoryField == null ? JsonNull.INSTANCE : serviceRepositoryField.toJsonObject());
        jsonObject.add("repository", repository == null ? JsonNull.INSTANCE : repository.toJsonObject());
        jsonObject.add("repositoryMethod", repositoryMethod == null ? JsonNull.INSTANCE : repositoryMethod.toJsonObject());

        return jsonObject;
    }

    public JsonObject toSmallJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("model", model == null ? "" : model.getName());
        jsonObject.addProperty("controller", controller == null ? "" : controller.getName());
        jsonObject.addProperty("controllerMethod", controllerMethod == null ? "" : controllerMethod.getName());
        jsonObject.addProperty("serviceMethodCall", serviceMethodCall == null ? "" : serviceMethodCall.getName());
        jsonObject.addProperty("service", service == null ? "" : service.getName());
        jsonObject.addProperty("serviceMethod", serviceMethod == null ? "" : serviceMethod.getName());
        jsonObject.addProperty("repositoryMethodCall", repositoryMethodCall == null ? "" : repositoryMethodCall.getName());
        jsonObject.addProperty("serviceRepositoryField", serviceRepositoryField == null ? "" : serviceRepositoryField.getName());
        jsonObject.addProperty("repository", repository == null ? "" : repository.getName());
        jsonObject.addProperty("repositoryMethod", repositoryMethod == null ? "" : repositoryMethod.getName());

        return jsonObject;
    }
}