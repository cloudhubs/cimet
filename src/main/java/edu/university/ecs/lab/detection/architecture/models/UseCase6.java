package edu.university.ecs.lab.detection.architecture.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Method;
import edu.university.ecs.lab.common.models.ir.MethodCall;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import edu.university.ecs.lab.detection.architecture.models.enums.Scope;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UseCase6 extends AbstractUseCase {
    protected static final String NAME = "Affected endpoint due to business logic update";
    protected static final Scope SCOPE = Scope.METHOD_CALL;
    protected static final String DESC = "A service method was modified and now causes inconsistent results for calling endpoints";
    protected JsonObject metaData;

    private UseCase6() {}

    @Override
    public List<? extends AbstractUseCase> checkUseCase() {
        // This method should return the list of UseCase3 instances relevant to UseCase7 logic, if any.
        ArrayList<UseCase3> useCases = new ArrayList<>();
        return useCases;
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

    public List<UseCase6> scan(Delta delta, JClass oldClass) {
        if(!delta.getChangeType().equals(ChangeType.MODIFY) || !delta.getClassChange().getClassRole().equals(ClassRole.SERVICE)) {
            return new ArrayList<>();
        }

        List<UseCase6> useCases = new ArrayList<>();
        Set<Method> affectedMethods = delta.getClassChange().getMethods();
        Method removeMethod = null;

        // For each methodCall added
        for(MethodCall methodCall : getNewMethodCalls(oldClass, delta.getClassChange())) {
            outer:
            {
                for (Method method : affectedMethods) {
                    // If the called object is the same as the return type of the same method
                    // TODO This will report the first instance
                    // TODO This currently does not check if the affected method is called by an endpoint and actually used, flows needed
                    if (method.getReturnType().equals(methodCall.getObjectName())
                            && method.getName().equals(methodCall.getCalledFrom())) {
                        removeMethod = method;

                        UseCase6 useCase7 = new UseCase6();
                        JsonObject jsonObject = new JsonObject();

                        jsonObject.add("AffectedMethod", method.toJsonObject());
                        jsonObject.add("MethodCall", methodCall.toJsonObject());

                        useCase7.setMetaData(jsonObject);
                        useCases.add(useCase7);

                        break outer;
                    }
                }
            }

            if(Objects.nonNull(removeMethod)) {
                affectedMethods.remove(removeMethod);
                removeMethod = null;
            }
        }

        return useCases;
    }

    /**
     * This method gets new method calls not present in oldClass
     *
     * @param oldClass
     * @param newClass
     * @return
     */
    private static Set<MethodCall> getNewMethodCalls(JClass oldClass, JClass newClass) {
        return newClass.getMethodCalls().stream().filter(methodCall -> !oldClass.getMethodCalls().contains(methodCall)).collect(Collectors.toSet());
    }
}
