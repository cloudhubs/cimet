package edu.university.ecs.lab;

import com.google.gson.JsonArray;
import edu.university.ecs.lab.common.models.JService;
import edu.university.ecs.lab.common.models.Microservice;
import edu.university.ecs.lab.common.models.MsSystem;
import edu.university.ecs.lab.common.models.RestCall;
import edu.university.ecs.lab.common.utils.IRParserUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import java.util.*;

public class TemporalRunner {
    private static final List<String> SERVICES = Arrays.asList("ts-admin-service",
            "ts-assurance-service",
            "ts-auth-service",
            "ts-cancel-service",
            "ts-config-service",
            "ts-consign-service",
            "ts-contacts-service",
            "ts-delivery-service",
            "ts-food-service",
            "ts-gateway",
            "ts-new-gateway",
            "ts-notification-service",
            "ts-order-related-service",
            "ts-order-service",
            "ts-preserve-service",
            "ts-price-service",
            "ts-rebook-service",
            "ts-route-service",
            "ts-security-service",
            "ts-station-service",
            "ts-travel-service",
            "ts-ui-dashboard",
            "ts-user-service");


    public static void main(String[] args) {
        int serviceCount = SERVICES.size();

        Map<Integer, Set<Integer>> networkMap = new HashMap<>(serviceCount);

        if(args.length != 1) {
            System.err.println("Usage: TemporalRunner <file>");
            System.exit(1);
        }

        String irPath = args[0];

        MsSystem system = IRParserUtils.parseIRSystem(irPath);

        for(Microservice microservice : system.getMsList()) {
            for(JService jService : microservice.getServices()) {
                for(RestCall restCall : jService.getRestCalls()) {
                    if(!restCall.getDestEndpoint().equals(RestCall.DEST_DELETED)) {
                        networkMap.computeIfAbsent(SERVICES.indexOf(restCall.getMsId()), k -> new HashSet<Integer>()).add(SERVICES.indexOf(restCall.getDestMsId()));
                    }
                }
            }
        }

        for(int i = 0; i < serviceCount; i++) {
            for(int j = 0; j < serviceCount; j++) {
                System.out.print(Objects.nonNull(networkMap.get(i)) && networkMap.get(i).contains(j) ? 1 : 0);
                System.out.print(" ");
            }

            System.out.println();
        }



    }



}
