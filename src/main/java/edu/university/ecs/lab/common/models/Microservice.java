package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.*;

/**
 * Represents the overarching structure of a microservice system. It is composed of classes which
 * hold all information in that class.
 */
@Data
@AllArgsConstructor
public class Microservice implements JsonSerializable {
  /** The name of the service (ex: "ts-assurance-service") */
  private String name;

  private String branch;

  /** The commit id of the service as cloned */
  private String commit;

  /** Controller classes belonging to the microservice. */
  private List<JClass> controllers;

  /** Service classes to the microservice. */
  private List<JClass> services;

  /** Repository classes belonging to the microservice. */
  private List<JClass> repositories;


  @Override
  public JsonObject toJsonObject() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("name", name);
    jsonObject.addProperty("commitId", commit);
    jsonObject.add("controllers", JsonSerializable.toJsonArray(controllers));
    jsonObject.add("services", JsonSerializable.toJsonArray(services));
    jsonObject.add("repositories", JsonSerializable.toJsonArray(repositories));

    return jsonObject;
  }

}
