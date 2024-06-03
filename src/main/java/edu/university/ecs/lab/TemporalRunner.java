//package edu.university.ecs.lab;
//
//import edu.university.ecs.lab.common.models.JService;
//import edu.university.ecs.lab.common.models.Microservice;
//import edu.university.ecs.lab.common.models.System;
//import edu.university.ecs.lab.common.models.RestCall;
//import edu.university.ecs.lab.common.utils.IRParserUtils;
//
//import java.util.*;
//
//public class TemporalRunner {
//    private static final List<String> SERVICES = Arrays.asList("ts-admin-service",
//            "ts-assurance-service",
//            "ts-auth-service",
//            "ts-cancel-service",
//            "ts-config-service",
//            "ts-consign-service",
//            "ts-contacts-service",
//            "ts-delivery-service",
//            "ts-food-service",
//            "ts-gateway",
//            "ts-new-gateway",
//            "ts-notification-service",
//            "ts-order-related-service",
//            "ts-order-service",
//            "ts-preserve-service",
//            "ts-price-service",
//            "ts-rebook-service",
//            "ts-route-service",
//            "ts-security-service",
//            "ts-station-service",
//            "ts-travel-service",
//            "ts-ui-dashboard",
//            "ts-user-service");
//
//
//    public static void main(String[] args) {
//        int serviceCount = SERVICES.size();
//
//        Map<Integer, Set<Integer>> networkMap = new HashMap<>(serviceCount);
//
//        if(args.length != 1) {
//            java.lang.System.err.println("Usage: TemporalRunner <file>");
//            java.lang.System.exit(1);
//        }
//
//        String irPath = args[0];
//
//        System system = IRParserUtils.parseIRSystem(irPath);
//
//        for(Microservice microservice : system.getMsList()) {
//            for(JService jService : microservice.getServices()) {
//                for(RestCall restCall : jService.getRestCalls()) {
//                    if(!restCall.getDestEndpoint().equals(RestCall.DEST_DELETED)) {
//                        networkMap.computeIfAbsent(SERVICES.indexOf(restCall.getMsId()), k -> new HashSet<Integer>()).add(SERVICES.indexOf(restCall.getDestMsId()));
//                    }
//                }
//            }
//        }
//
//        for(int i = 0; i < serviceCount; i++) {
//            for(int j = 0; j < serviceCount; j++) {
//                java.lang.System.out.print(Objects.nonNull(networkMap.get(i)) && networkMap.get(i).contains(j) ? 1 : 0);
//                java.lang.System.out.print(" ");
//            }
//
//            java.lang.System.out.println();
//        }
//
//
//
//    }
//
//
//
//}
