package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

import static edu.university.ecs.lab.common.utils.ObjectToJsonUtils.listToJsonArray;

/**
 * Represents the overarching structure of a microservice system. It is composed of classes which
 * hold all information in that class.
 */
@Data
@AllArgsConstructor
public class Microservice implements JsonSerializable {
  /** The name of the service (ex: "ts-assurance-service") */
  @SerializedName("id")
  private String id;

  @SerializedName("branch")
  private String branch;

  /** The commit id of the service as cloned */
  @SerializedName("commitId")
  private String commit;

  /** Controller classes belonging to the microservice. */
  private List<JClass> controllers;

  /** Service classes to the microservice. */
  private List<JClass> services;

  /** DTO classes belonging to the microservice. */
  private List<JClass> dtos;

  /** Repository classes belonging to the microservice. */
  private List<JClass> repositories;

  /** Entity classes belonging to the microservice. */
  private List<JClass> entities;

  /**
   * Constructor for the microservice object with all lists as empty
   *
   * @param id the name of the service
   * @param commit the commit # of the service
   */
  public Microservice(String id, String commit) {
    this.setId(id);
    this.setCommit(commit);
    this.setControllers(new ArrayList<>());
    this.setServices(new ArrayList<>());
    this.setRepositories(new ArrayList<>());
    this.setDtos(new ArrayList<>());
    this.setEntities(new ArrayList<>());
  }

  @Override
  public JsonObject toJsonObject() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    builder.add("id", id);
    builder.add("commitId", commit);
    builder.add("controllers", listToJsonArray(controllers));
    builder.add("services", listToJsonArray(services));
    builder.add("dtos", listToJsonArray(services));
    builder.add("repositories", listToJsonArray(repositories));
    builder.add("entities", listToJsonArray(entities));

    return builder.build();
  }

}
