package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the overarching structure of a microservice system. It is composed of classes which
 * hold all information in that class.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Microservice implements JsonSerializable {
    /**
     * The name of the service (ex: "ts-assurance-service")
     */
    private String name;

    /**
     * The path to the folder that represents the microservice
     */
    private String path;

    /**
     * Controller classes belonging to the microservice.
     */
    private final Set<JClass> controllers;

    /**
     * Service classes to the microservice.
     */
    private final Set<JClass> services;

    /**
     * Repository classes belonging to the microservice.
     */
    private final Set<JClass> repositories;

    /**
     * Entity classes belonging to the microservice.
     */
    private final Set<JClass> entities;

    /**
     * Embeddable classes belonging to the microservice.
     */
    private final Set<JClass> embeddables;

    /**
     * Feign client classes belonging to the microservice.
     */
    private final Set<JClass> feignClients;

    /**
     * Static files belonging to the microservice.
     */
    private final Set<JsonObject> files;

    public Microservice(String name, String path) {
        this.name = name;
        this.path = path;
        this.controllers = new HashSet<>();
        this.services = new HashSet<>();
        this.repositories = new HashSet<>();
        this.entities = new HashSet<>();
        this.embeddables = new HashSet<>();
        this.feignClients = new HashSet<>();
        this.files = new HashSet<>();
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", name);
        jsonObject.addProperty("path", path);
        jsonObject.add("controllers", JsonSerializable.toJsonArray(controllers));
        jsonObject.add("entities", JsonSerializable.toJsonArray(entities));
        jsonObject.add("embeddables", JsonSerializable.toJsonArray(embeddables));
//        jsonObject.add("feignClients", JsonSerializable.toJsonArray(feignClients));
        jsonObject.add("services", JsonSerializable.toJsonArray(services));
        jsonObject.add("repositories", JsonSerializable.toJsonArray(repositories));
        jsonObject.add("files", toJsonArray(files));

        return jsonObject;
    }


    private static JsonArray toJsonArray(Iterable<JsonObject> list) {
        JsonArray jsonArray = new JsonArray();
        for (JsonObject object : list) {
            jsonArray.add(object);
        }
        return jsonArray;
    }

    public void addJClass(JClass jClass) {
        switch (jClass.getClassRole()) {
            case CONTROLLER:
                controllers.add(jClass);
                break;
            case SERVICE:
                services.add(jClass);
                break;
            case REPOSITORY:
                repositories.add(jClass);
                break;
            case ENTITY:
                entities.add(jClass);
                break;
            case EMBEDDABLE:
                embeddables.add(jClass);
                break;
            case FEIGN_CLIENT:
                feignClients.add(jClass);
                break;


        }
    }

    /**
     * This method removes a JClass from the microservice
     * by looking up it's path
     *
     * @param path the path to search for removal
     */

    public void removeJClass(String path) {
        Set<JClass> classes = getClasses();
        JClass removeClass = null;

        for (JClass jClass : classes) {
            if (jClass.getPath().equals(path)) {
                removeClass = jClass;
                break;
            }
        }

        // If we cannot find the class no problem, we will skip it quietly
        if (removeClass == null) {
            return;
        }

        switch (removeClass.getClassRole()) {
            case CONTROLLER:
                controllers.remove(removeClass);
                break;
            case SERVICE:
                services.remove(removeClass);
                break;
            case REPOSITORY:
                repositories.remove(removeClass);
                break;
            case ENTITY:
                entities.remove(removeClass);
                break;
            case EMBEDDABLE:
                embeddables.remove(removeClass);
                break;
            case FEIGN_CLIENT:
                feignClients.remove(removeClass);
                break;
        }
    }

    /**
     * This method returns all classes of the microservice in a new set
     *
     * @return the set of all JClasses
     */
    public Set<JClass> getClasses() {
        Set<JClass> classes = new HashSet<>();
        classes.addAll(controllers);
        classes.addAll(services);
        classes.addAll(repositories);
        classes.addAll(entities);
        classes.addAll(embeddables);
        classes.addAll(feignClients);

        return classes;
    }

}
