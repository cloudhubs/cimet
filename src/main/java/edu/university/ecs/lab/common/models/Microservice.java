package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * Represents the overarching structure of a microservice system. It is composed of classes which
 * hold all information in that class.
 */
@Data
//@AllArgsConstructor
@EqualsAndHashCode
public class Microservice implements JsonSerializable {
  /** The name of the service (ex: "ts-assurance-service") */
  private String name;

  /** The path to the folder that represents the microservice */
  private String path;

//  private String branch;
//
//  /** The commit id of the service as cloned */
//  private String commit;

  /** Controller classes belonging to the microservice. */
  private final Set<JClass> controllers;

  /** Service classes to the microservice. */
  private final Set<JClass> services;

  /** Repository classes belonging to the microservice. */
  private final Set<JClass> repositories;

  /** Repository classes belonging to the microservice. */
  private final Set<JClass> entities;

  public Microservice(String name, String path) {
    this.name = name;
    this.path = path;
    this.controllers = new HashSet<>();
    this.services = new HashSet<>();
    this.repositories = new HashSet<>();
    this.entities = new HashSet<>();
  }

  public Microservice(String name, String path, Set<JClass> controllers, Set<JClass> services, Set<JClass> repositories, Set<JClass> entities) {
    this.name = name;
    this.path = path;
    this.controllers = controllers;
    this.services = services;
    this.repositories = repositories;
    this.entities = entities;
  }


  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("name", name);
    jsonObject.addProperty("path", path);
//    jsonObject.addProperty("commitId", commit);
    jsonObject.add("controllers", JsonSerializable.toJsonArray(controllers));
    jsonObject.add("entities", JsonSerializable.toJsonArray(entities));
    jsonObject.add("services", JsonSerializable.toJsonArray(services));
    jsonObject.add("repositories", JsonSerializable.toJsonArray(repositories));

    return jsonObject;
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
    }
  }

  public void removeClass(String path) {
    List<JClass> classes = getClasses();
    JClass removeClass = null;

    for(JClass jClass : classes) {
      if(jClass.getClassPath().equals(path)) {
        removeClass = jClass;
        break;
      }
    }

    if(removeClass == null) {
      System.out.println("REMOVECLASS NOT FOUND");
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
    }
  }

  public List<JClass> getClasses() {
    List<JClass> classes = new ArrayList<>();
    classes.addAll(controllers);
    classes.addAll(services);
    classes.addAll(repositories);
    classes.addAll(entities);

    return classes;
  }

}
