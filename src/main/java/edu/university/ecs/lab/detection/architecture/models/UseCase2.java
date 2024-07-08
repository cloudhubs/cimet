//package edu.university.ecs.lab.detection.architecture.models;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import com.google.gson.JsonObject;
//
//import edu.university.ecs.lab.common.models.enums.ClassRole;
//import edu.university.ecs.lab.common.models.ir.*;
//import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
//import edu.university.ecs.lab.delta.models.Delta;
//import edu.university.ecs.lab.delta.models.enums.ChangeType;
//import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
//import lombok.Data;
//
//@Data
//public class UseCase2 extends AbstractUseCase {
//    protected static final String TYPE = "UseCase2";
//    protected static final String NAME = "Floating call due to endpoint removal (external)";
//    protected static final Scope SCOPE = Scope.ENDPOINT;
//    protected static final String DESC = "An endpoint was removed and calls now reference an endpoint no longer in existence.";
//    private String oldCommitID;
//    private String newCommitID;
//    protected JsonObject metaData;
//
//    @Override
//    public List<? extends AbstractUseCase> checkUseCase() {
//        ArrayList<UseCase3> useCases = new ArrayList<>();
//
//        return new ArrayList<>();
//    }
//
//    @Override
//    public String getName() {
//        return NAME;
//    }
//
//    @Override
//    public String getDescription() {
//        return DESC;
//    }
//
//    @Override
//    public Scope getScope() {
//        return SCOPE;
//    }
//
//    @Override
//    public double getWeight() {
//        return 0;
//    }
//
//    @Override
//    public JsonObject getMetaData() {
//        return metaData;
//    }
//
//    @Override
//    public String getType() {
//        return TYPE;
//    }
//
//    public static List<UseCase2> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
//        List<UseCase2> useCases = new ArrayList<>();
//
//        // If we are not deleting/modifying a controller class
//        JClass oldClass = oldSystem.findClass(delta.getOldPath());
//
//        if (delta.getChangeType().equals(ChangeType.ADD) || !oldClass.getClassRole().equals(ClassRole.CONTROLLER)) {
//            return useCases;
//        }
//
//        // For each endpoint in the old class, if it's no longer found
//        for (Endpoint endpoint : oldClass.getEndpoints()) {
//            if (!existsInSystem(endpoint, newSystem)) {
//                UseCase2 useCase2 = new UseCase2();
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.add("Endpoint", endpoint.toJsonObject());
//                useCase2.setMetaData(jsonObject);
//                useCase2.setOldCommitID(oldSystem.getCommitID());
//                useCase2.setNewCommitID(newSystem.getCommitID());
//                useCases.add(useCase2);
//
//            }
//        }
//        return useCases;
//    }
//
////    public static List<UseCase2> scan2(MicroserviceSystem newSystem) {
////        List<UseCase2> useCases = new ArrayList<>();
////
////        // For each endpoint in the old class, if it's no longer found
////        Set<JClass> controllers = newSystem.getMicroservices().stream().flatMap(microservice -> microservice.getControllers().stream()).collect(Collectors.toSet());
////        controllers.addAll(newSystem.getOrphans().stream().filter(jClass -> jClass.getClassRole().equals(ClassRole.CONTROLLER)).collect(Collectors.toSet()));
////
////        Set<JClass> services = newSystem.getMicroservices().stream().flatMap(microservice -> microservice.getServices().stream()).collect(Collectors.toSet());
////        services.addAll(newSystem.getOrphans().stream().filter(jClass -> jClass.getClassRole().equals(ClassRole.CONTROLLER)).collect(Collectors.toSet()));
////        Set<RestCall> floatingCalls = new HashSet<>();
////
////
////        for(JClass service : services) {
////            for(RestCall restCall : service.getRestCalls()) {
////                outer:
////                {
////                    for (JClass controller : controllers) {
////                        for (Endpoint endpoint : controller.getEndpoints()) {
////                            if (!restCall.getMicroserviceName().equals(endpoint.getMicroserviceName()) &&
////                                    RestCall.matchEndpoint(restCall, endpoint)) {
////                                break outer;
////                            }
////                        }
////                    }
////                    UseCase2 useCase2 = new UseCase2();
////                    JsonObject jsonObject = new JsonObject();
////                    jsonObject.add("RestCall", restCall.toJsonObject());
////                    useCase2.setMetaData(jsonObject);
////
////                    useCases.add(useCase2);
////
////                }
////            }
////        }
////
////
////        return useCases;
////    }
//
//    // Check for modified/deleted endpoint in new system
//    private static boolean existsInSystem(Endpoint endpoint, MicroserviceSystem newSystem) {
//        for (Microservice microservice : newSystem.getMicroservices()) {
//            for (JClass controller : microservice.getControllers()) {
//                for (Endpoint endP : controller.getEndpoints()) {
//                    if (endpoint.equals(endP)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    // Check for modified/deleted endpoint in new system
//    private static List<RestCall> getAffectedRestCalls(Endpoint endpoint, MicroserviceSystem oldSystem) {
//        List<RestCall> restCalls = new ArrayList<>();
//        for (Microservice microservice : oldSystem.getMicroservices()) {
//            for (JClass service : microservice.getServices()) {
//                for (RestCall restCall : service.getRestCalls()) {
//                    if(RestCall.matchEndpoint(restCall, endpoint)) {
//                        restCalls.add(restCall);
//                    }
//                }
//            }
//        }
//
//        return restCalls;
//    }
//}
