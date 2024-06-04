//package edu.university.ecs.lab.delta.models;
//
//import com.google.gson.annotations.SerializedName;
//import edu.university.ecs.lab.common.models.JsonSerializable;
//import lombok.AllArgsConstructor;
//
//import javax.json.Json;
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static edu.university.ecs.lab.common.utils.ObjectToJsonUtils.listToJsonArray;
//
///** DTO for {@link SystemChange}, as we write the maps as lists instead. */
//@AllArgsConstructor
//public class SystemChangeDTO implements JsonSerializable {
//
//  @SerializedName("controllers")
//  private List<Delta> controllers;
//
//  @SerializedName("services")
//  private List<Delta> services;
//
//  @SerializedName("dtos")
//  private List<Delta> dtos;
//
//  @SerializedName("repositories")
//  private List<Delta> repositories;
//
//  @SerializedName("entities")
//  private List<Delta> entities;
//
//  /**
//   * Convert a {@link SystemChange} to a {@link SystemChangeDTO}.
//   *
//   * @param systemChange the system change to convert
//   */
//  public SystemChangeDTO(SystemChange systemChange) {
//    this.controllers = new ArrayList<>(systemChange.getControllers().values());
//    this.services = new ArrayList<>(systemChange.getServices().values());
//    this.dtos = new ArrayList<>(systemChange.getDtos().values());
//    this.repositories = new ArrayList<>(systemChange.getRepositories().values());
//    this.entities = new ArrayList<>(systemChange.getEntities().values());
//  }
//
//  /**
//   * Convert this DTO to a {@link SystemChange}.
//   *
//   * @return the system change
//   */
//  public SystemChange toSystemChange() {
//    return new SystemChange(
//        controllers.stream().collect(Collectors.toMap(Delta::getLocalPath, delta -> delta)),
//        services.stream().collect(Collectors.toMap(Delta::getLocalPath, delta -> delta)),
//        dtos.stream().collect(Collectors.toMap(Delta::getLocalPath, delta -> delta)),
//        repositories.stream().collect(Collectors.toMap(Delta::getLocalPath, delta -> delta)),
//        entities.stream().collect(Collectors.toMap(Delta::getLocalPath, delta -> delta)));
//  }
//
//  @Override
//  public JsonObject toJsonObject() {
//    JsonObjectBuilder finalOutputBuilder = Json.createObjectBuilder();
//    finalOutputBuilder.add("controllers", listToJsonArray(controllers));
//    finalOutputBuilder.add("services", listToJsonArray(services));
//    finalOutputBuilder.add("repositories", listToJsonArray(repositories));
//    finalOutputBuilder.add("dtos", listToJsonArray(dtos));
//    finalOutputBuilder.add("entities", listToJsonArray(entities));
//    return finalOutputBuilder.build();
//  }
//}
