//package edu.university.ecs.lab.intermediate.utils;
//
//import edu.university.ecs.lab.common.models.JController;
//import edu.university.ecs.lab.common.models.JService;
//import edu.university.ecs.lab.common.models.ir.Microservice;
//
//import java.util.Map;
//
//public class IRParserUtils {
//  /**
//   * Iterate over extracted services and link together rest calls to their destination endpoints
//   *
//   * @param msMap a map of service to their information
//   */
//  public static void updateCallDestinations(Map<String, Microservice> msMap) {
//    for (Microservice src : msMap.values()) {
//      for (JController controller : src.getControllers()) {
//        for (Microservice dest : msMap.values()) {
//          if (dest != src) {
//            for (JService service : dest.getServices()) {
//              service
//                  .getRestCalls()
//                  .forEach(
//                      restCall -> {
//                        // TODO this doesnt work due to duplicate urls with different base paths
//                        controller.getEndpoints().stream()
//                            .filter(e -> e.matchCall(restCall))
//                            .findAny()
//                            .ifPresent(
//                                endpoint -> {
//                                  restCall.setDestination(controller);
//                                  endpoint.addCall(restCall, service);
//                                });
//                      });
//            }
//          }
//        }
//      }
//    }
//  }
//}
