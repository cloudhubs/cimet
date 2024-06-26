package edu.university.ecs.lab.detection.architecture.models;

import java.util.*;

import com.google.gson.JsonObject;

import edu.university.ecs.lab.common.models.ir.Endpoint;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.ir.RestCall;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

@Data
public class UseCase4 extends AbstractUseCase {

    protected static final String NAME = "Floating endpoint due to last call removal";
    protected static final Scope SCOPE = Scope.REST_CALL;
    protected static final String DESC = "Any rest calls referencing an endpoint are now gone. This endpoint is now unused by any other microservice";
    protected JsonObject metaData;

    private UseCase4() {}

    @Override
    public List<? extends AbstractUseCase> checkUseCase() {
        // To be implemented if needed
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public Scope getScope() {
        return SCOPE;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public JsonObject getMetaData() {
        return metaData;
    }

    public static List<UseCase4> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {
        List<UseCase4> useCases = new ArrayList<>();

        // If we are not removing or modifying a service
        if (delta.getChangeType().equals(ChangeType.ADD) || !delta.getClassChange().getClassRole().equals(ClassRole.SERVICE)) {
            return useCases;
        }

        // Get endpoints that do not have any calls
        Set<Endpoint> uncalledEndpoints = getEndpointsWithNoCalls(newSystem);

        JClass oldClass;
        Set<RestCall> restCalls = new HashSet<>();

        if (delta.getChangeType().equals(ChangeType.MODIFY)) {
            oldClass = oldSystem.findClass(delta.getNewPath());
            restCalls = getRemovedRestCalls(delta, oldClass);
        } else if (delta.getChangeType().equals(ChangeType.DELETE)) {
            oldClass = oldSystem.findClass(delta.getOldPath());
            restCalls = oldClass.getRestCalls();
        }

        Endpoint removeEndpoint = null;
        // For each restCall removed
        for (RestCall restCall : restCalls) {
            // TODO this approach picks the first rest call that no longer calls an uncalled endpoint
            endpointLoop:
            {
                // For endpoint with no call
                for (Endpoint endpoint : uncalledEndpoints) {
                    // If we match, they once called but no longer call
                    if (RestCall.matchEndpoint(restCall, endpoint)) {
                        UseCase4 useCase4 = new UseCase4();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.add("RestCall", restCall.toJsonObject());
                        useCase4.setMetaData(jsonObject);
                        useCases.add(useCase4);
                        removeEndpoint = endpoint;
                        break endpointLoop;
                    }
                }
            }

            // Update endpoint loop as we potentially found a match
            if(Objects.nonNull(removeEndpoint)) {
                uncalledEndpoints.remove(removeEndpoint);
            }
            removeEndpoint = null;
        }


        return useCases;
    }

    /**
     * This method collects rest calls that were modified and are no longer present
     * in the new system.
     *
     * @param delta delta change associated
     * @param oldClass the delta changed class from the oldSystem
     * @return a set of rest calls
     */
    private static Set<RestCall> getRemovedRestCalls(Delta delta, JClass oldClass){
        Set<RestCall> removedRestCalls = new HashSet<>();

        // For the restCalls in delta
        for(RestCall modifiedRestCall: delta.getClassChange().getRestCalls()){
            outer: {
                // For the restCalls in oldClass
                for(RestCall existingRestCall: oldClass.getRestCalls()){
                    // If they match we do not have a removed call
                    if(existingRestCall.equals(modifiedRestCall)){
                        break outer;
                    }
                }

                // If we loop through them all and find no match add them to the removed calls
                removedRestCalls.add(modifiedRestCall);
            }
        }

        return removedRestCalls;
    }

    /**
     * This method generates a list of endpoints that have no rest calls
     *
     * @param newSystem the new system to check for endpoints
     * @return a list of endpoints
     */
    private static Set<Endpoint> getEndpointsWithNoCalls(MicroserviceSystem newSystem) {
        Set<Endpoint> endpointsNoRC = new HashSet<>();

        for (Microservice microservice : newSystem.getMicroservices()) {
            for (JClass controller : microservice.getControllers()) {
                for (JClass service : microservice.getServices()) {
                    for (Endpoint endpoint : controller.getEndpoints()) {
                        boolean hasRestCall = false;
                        for (RestCall restCall : service.getRestCalls()) {
                            if (RestCall.matchEndpoint(restCall, endpoint)) {
                                hasRestCall = true;
                                break;
                            }
                        }
                        if (!hasRestCall) {
                            endpointsNoRC.add(endpoint);
                        }
                    }
                }
            }
        }

        return endpointsNoRC;
    }
}
