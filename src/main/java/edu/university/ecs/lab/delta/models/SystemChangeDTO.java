//package edu.university.ecs.lab.delta.models;
//
//import com.google.gson.JsonObject;
//import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//
///** DTO for {@link SystemChange}, as we write the maps as lists instead. */
//@Data
//@AllArgsConstructor
//public class SystemChangeDTO implements JsonSerializable {
//
//  private List<Delta> controllers;
//
//  private List<Delta> services;
//
//  private List<Delta> repositories;
//
//
//  /**
//   * Convert a {@link SystemChange} to a {@link SystemChangeDTO}.
//   *
//   * @param systemChange the system change to convert
//   */
//  public SystemChangeDTO(SystemChange systemChange) {
//    this.controllers = new ArrayList<>(systemChange.getControllers().values());
//    this.services = new ArrayList<>(systemChange.getServices().values());
//    this.repositories = new ArrayList<>(systemChange.getRepositories().values());
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
//        repositories.stream().collect(Collectors.toMap(Delta::getLocalPath, delta -> delta)));
//  }
//
//  @Override
//  public JsonObject toJsonObject() {
//      JsonObject jsonObject = new JsonObject();
//
//      jsonObject.add("controllers", JsonSerializable.toJsonArray(getControllers()));
//      jsonObject.add("services", JsonSerializable.toJsonArray(getServices()));
//      jsonObject.add("repositories", JsonSerializable.toJsonArray(getRepositories()));
//
//    return jsonObject;
//  }
//}
