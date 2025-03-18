package edu.university.ecs.lab.detection.models.results.architecture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Method;
import edu.university.ecs.lab.common.models.ir.MethodCall;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * Architectural Rule 6 Class: Affected endpoint due to business logic update
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AR6 extends AbstractAR {

    /**
     * Architectural rule 6 details
     */
    protected static final String TYPE = "Architectural Rule 6";
    protected static final String NAME = "Affected endpoint due to business logic update";
    protected static final String DESC = "A service method was modified and now causes inconsistent results for calling endpoints";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Scan and compare old microservice system and new microservice system to identify endpoints affected by business logic update
     * 
     * @param delta change between old commit and new microservice systems
     * @param oldSystem old commit of microservice system
     * @param newSystem new commit of microservice system
     * @return list of calls to modified methods (first instance)
     */
    public static List<AR6> scan(Delta delta, MicroserviceSystem oldSystem, MicroserviceSystem newSystem) {


        JClass jClass = oldSystem.findClass(delta.getOldPath());
        if(jClass == null) {
            return new ArrayList<>();
        }

        // Return empty list if it isn't modify or not a service
        if(!delta.getChangeType().equals(ChangeType.MODIFY) || !jClass.getClassRole().equals(ClassRole.SERVICE)) {
            return new ArrayList<>();
        }

        List<MethodCall> uniqueMethodCalls = getUniqueMethodCalls(jClass.getMethodCalls(), delta.getClassChange().getMethodCalls());

        List<AR6> archRules = new ArrayList<>();

        // For each methodCall added
        for(Method method : delta.getClassChange().getMethods()) {
            outer:
            {

                for (MethodCall methodCall : uniqueMethodCalls) {
                    // If the called object is the same as the return type of the same method
                    // TODO This will report the first instance
                    // TODO This currently does not check if the affected method is called by an endpoint and actually used, flows needed
                    if (method.getReturnType().equals(methodCall.getObjectType())
                            && method.getName().equals(methodCall.getCalledFrom())) {

                        AR6 archRule6 = new AR6();
                        ObjectMapper objMapper = new ObjectMapper();
                        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
                        jsonObject.set("AffectedMethod", objMapper.valueToTree(method));
                        jsonObject.set("MethodCall", objMapper.valueToTree(methodCall));

                        archRule6.setOldCommitID(oldSystem.getCommitID());
                        archRule6.setNewCommitID(newSystem.getCommitID());

                        archRule6.setMetaData(jsonObject);
                        archRules.add(archRule6);

                        break outer;
                    }
                }
            }

        }

        return archRules;
    }

    // Define a custom comparator for the MethodCall class
    private static Comparator<MethodCall> methodCallComparator = Comparator
            .comparing(MethodCall::getName)
            .thenComparing(MethodCall::getParameterContents)
            .thenComparing(MethodCall::getObjectType)
            .thenComparing(MethodCall::getCalledFrom);

    public static List<MethodCall> getUniqueMethodCalls(List<MethodCall> oldMethodCalls, List<MethodCall> newMethodCalls) {
        // Using TreeSet with a custom comparator to enforce uniqueness based on custom logic
        List<MethodCall> final1 = new ArrayList<>();


        boolean foundMatch = false;
        for(MethodCall oldmethodCall : oldMethodCalls) {
            outer:
            {
                for (MethodCall newMethodCall : newMethodCalls) {
                    if (methodCallComparator.compare(oldmethodCall, newMethodCall) == 0) {
                        foundMatch = true;
                        break outer;
                    }
                }

                if(!foundMatch) {
                    final1.add(oldmethodCall);
                }
                foundMatch = false;
            }
        }

        for(MethodCall newMethodCall : newMethodCalls) {
            outer:
            {
                for (MethodCall oldmethodCall : oldMethodCalls) {
                    if (methodCallComparator.compare(oldmethodCall, newMethodCall) == 0) {
                        foundMatch = true;
                        break outer;
                    }
                }

                if(!foundMatch) {
                    final1.add(newMethodCall);
                }
                foundMatch = false;
            }
        }


        return final1;
    }

}
