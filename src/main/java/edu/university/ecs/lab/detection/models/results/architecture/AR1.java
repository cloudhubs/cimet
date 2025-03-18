//package edu.university.ecs.lab.detection.models.results.architecture;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//
//import edu.university.ecs.lab.common.models.enums.ClassRole;
//import edu.university.ecs.lab.common.models.ir.*;
//import edu.university.ecs.lab.common.utils.FlowUtils;
//import edu.university.ecs.lab.delta.models.Delta;
//import edu.university.ecs.lab.delta.models.enums.ChangeType;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
///**
// * Architectural Rule 1 Class: Floating call due to endpoint removal (internal)
// */
//@EqualsAndHashCode(callSuper = true)
//@Data
//public class AR1 extends AbstractAR {
//
//    /**
//     * Architectural rule 1 details
//     */
//    protected static final String TYPE = "Architectural Rule 1";
//    protected static final String NAME = "Floating call due to endpoint removal (internal)";
//    protected static final String DESC = "An endpoint was removed, inter service calls depending on this method are no longer called";
//
//    @Override
//    @JsonIgnore
//    public String getName() {
//        return NAME;
//    }
//
//    @Override
//    @JsonIgnore
//    public String getDescription() {
//        return DESC;
//    }
//
//    @Override
//    @JsonIgnore
//    public double getWeight() {
//        return 0;
//    }
//
//    @Override
//    public String getType() {
//        return TYPE;
//    }
//
//    /**
//     * Scan and compare old microservice system and new microservice system to identify endpoint removals
//     *
//     * @param delta change between old commit and new microservice systems
//     * @param oldSystem old commit of microservice system
//     * @param newSystem new commit of microservice system
//     * @return list of restCalls called in severed flow (if method was deleted)
//     */
//    public static List<AR1> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem){
//       List<AR1> useCases = new ArrayList<>();
//
//      // Old class for delete, delta class for modify
//      JClass jClass = delta.getChangeType().equals(ChangeType.MODIFY) ? delta.getClassChange() : oldSystem.findClass(delta.getOldPath());
//
//      if(delta.getChangeType().equals(ChangeType.ADD) || !jClass.getClassRole().equals(ClassRole.CONTROLLER)) {
//          return useCases;
//      }
//
//      List<Flow> flows = FlowUtils.buildFlows(oldSystem);
//      Endpoint endpointMatch = null;
//      for(Flow flow : flows){
//          if(flow.getController() != null && flow.getController().getPath().equals(delta.getOldPath())){
//              for(Endpoint endpoint : jClass.getEndpoints()) {
//                  if(endpoint.getName().equals(flow.getControllerMethod().getName())){
//                      endpointMatch = endpoint;
//                  }
//              }
//              // If we find no match or its a delete, we have removed this method
//              if((Objects.nonNull(endpointMatch) || delta.getChangeType().equals(ChangeType.DELETE)) && flow.getService() != null){
//                  for(RestCall restCall : flow.getService().getRestCalls()) {
//                      // If this restCall is called in the severed flow
//                      if(flow.getServiceMethod() != null && restCall.getCalledFrom().equals(flow.getServiceMethod().getName())){
//                          AR1 useCase1 = new AR1();
//                          JsonNode jsonObject = JsonNodeFactory.instance.objectNode();
//                            jsonObject.add("RestCall", restCall.toJsonObject());
//                            jsonObject.add("Endpoint", endpointMatch == null ? new JsonObject() : endpointMatch.toJsonObject());
//                          useCase1.setMetaData(jsonObject);
//                          useCase1.setOldCommitID(oldSystem.getCommitID());
//                          useCase1.setNewCommitID(newSystem.getCommitID());
//                          useCases.add(useCase1);
//                      }
//                  }
//              }
//
//              endpointMatch = null;
//          }
//
//
//      }
//
//      // Return list of restCalls called in severed flow (if method was deleted)
//      return useCases;
//  }
//
//  /**
//   * Scan and compare old microservice system and new microservice system to identify endpoint removals
//   *
//   * @param delta change between old commit and new microservice systems
//   * @param oldSystem old commit of microservice system
//   * @param newSystem new commit of microservice system
//   * @return list of restCalls not in any flows
//   */
//  public static List<AR1> scan2(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem){
//      List<AR1> useCases = new ArrayList<>();
//
//      List<Flow> flows = FlowUtils.buildFlows(oldSystem);
//      List<RestCall> allRestCalls = newSystem.getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).flatMap(jClass -> jClass.getRestCalls().stream()).collect(Collectors.toList());
//
//      for(RestCall restCall : allRestCalls) {
//          outer:
//          {
//              for (Flow flow : flows) {
//                  if (Objects.nonNull(flow.getRepositoryMethodCall()) && flow.getRepositoryMethodCall().equals(restCall)) {
//                      break outer;
//                  }
//              }
//
//              // If the restCall is not in any flows (flows start at controller)
//              AR1 archRule1 = new AR1();
//              ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
//              jsonObject.put("RestCall", restCall.getID());
//              archRule1.setMetaData(jsonObject);
//              archRule1.setOldCommitID(oldSystem.getCommitID());
//              archRule1.setNewCommitID(newSystem.getCommitID());
//              useCases.add(archRule1);
//
//          }
//      }
//
//      // Return list of restCalls not in any flows
//      return useCases;
//  }
//}
//