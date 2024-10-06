package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.models.ir.*;

import java.util.*;
import java.util.stream.Collectors;

public class FlowUtils {

    /**
     * Method for generating all possibilities
     * of Flows
     *
     * @param microserviceSystem the microservice system to scan for flows
     * @return the list of all possible flows
     */
    public static List<Flow> buildFlows(MicroserviceSystem microserviceSystem) {
        List<Flow> allFlows = new ArrayList<>();
        List<Flow> baseFlows = generateNewFlows(getAllMicroserviceControllers(microserviceSystem));
        Flow flowCopy1, flowCopy2, flowCopy3, flowCopy4;

        for(Flow flow : baseFlows) {
            List<MethodCall> serviceMethodCalls = findAllServiceMethodCalls(flow);
            for(MethodCall serviceMethodCall : serviceMethodCalls) {
                flowCopy1 = flow;
                flowCopy1.setServiceMethodCall(serviceMethodCall);

                Optional<Field> serviceField = Optional.ofNullable(findServiceField(flowCopy1));
                if (serviceField.isPresent()) {
                    flowCopy1.setControllerServiceField(serviceField.get());

                    List<JClass> serviceClasses = findAllServices(flowCopy1);
                    for (JClass serviceClass : serviceClasses) {
                        flowCopy2 = flowCopy1;
                        flowCopy2.setService(serviceClass);

                        Optional<Method> serviceMethod = Optional.ofNullable(findServiceMethod(flowCopy2));
                        if (serviceMethod.isPresent()) {
                            flowCopy2.setServiceMethod(serviceMethod.get());

                            List<MethodCall> repositoryMethodCalls = findAllRepositoryMethodCalls(flowCopy2);
                            for (MethodCall repositroyMethodCall : repositoryMethodCalls) {
                                flowCopy3 = flowCopy2;
                                flow.setRepositoryMethodCall(repositroyMethodCall);

                                Optional<Field> repositoryField = Optional.ofNullable(findRepositoryField(flowCopy3));
                                if (repositoryField.isPresent()) {
                                    flowCopy3.setServiceRepositoryField(repositoryField.get());

                                    List<JClass> repositoryClasses = findAllRepositorys(flowCopy3);
                                    for (JClass repositoryClass : repositoryClasses) {
                                        flowCopy4 = flowCopy3;

                                        flowCopy4.setRepository(repositoryClass);

                                        Optional<Method> repositoryMethod = Optional.ofNullable(findRepositoryMethod(flowCopy4));
                                        if (repositoryMethod.isPresent()) {
                                            flowCopy4.setRepositoryMethod(repositoryMethod.get());
                                        }

                                        allFlows.add(flowCopy4);
                                    }
                                } else {
                                    allFlows.add(flowCopy3);
                                }

                            }
                        } else {
                            allFlows.add(flowCopy2);
                        }

                    }
                } else {
                    allFlows.add(flowCopy1);
                }

            }

            if(serviceMethodCalls.isEmpty()) {
                allFlows.add(flow);
            }

        }

        return allFlows;
    }

    /**
     * This method returns a map of microservices to their controller classes
     *
     * @param microserviceSystem the microservice system to convert
     * @return the map of microservice to JClass controllers
     */
    private static Map<Microservice, Set<JClass>> getAllMicroserviceControllers(MicroserviceSystem microserviceSystem) {
        Map<Microservice, Set<JClass>> controllerMap = new HashMap<>();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            controllerMap.put(microservice, microservice.getControllers());
        }

        return controllerMap;
    }

    /**
     * This method generates the base flows
     *
     * @param controllerMap the controller map
     * @return the base flows
     */
    private static List<Flow> generateNewFlows(Map<Microservice, Set<JClass>> controllerMap) {
        List<Flow> flows = new ArrayList<>();
        Flow f;

        for (Map.Entry<Microservice, Set<JClass>> controllerList : controllerMap.entrySet()) {
            for (JClass controller : controllerList.getValue()) {
                for (Endpoint endpoint : controller.getEndpoints()) {
                    f = new Flow();
                    f.setController(controller);
                    f.setControllerMethod(endpoint);
                    f.setModel(controllerList.getKey());
                    flows.add(f);
                }
            }
        }
        return flows;
    }

    private static List<Flow> generateNewFlows(
            Microservice microservice, List<JClass> controllers) {
        List<Flow> flows = new ArrayList<>();
        Flow f;

        for (JClass controller : controllers) {
            for (Endpoint endpoint : controller.getEndpoints()) {
                f = new Flow();
                f.setController(controller);
                f.setControllerMethod(endpoint);
                f.setModel(microservice);
                flows.add(f);
            }
        }

        return flows;
    }

    /**
     * This method find's all method calls from the controllerMethod of a flow
     *
     * @param flow the flow
     * @return the list of MethodCalls from the controllerMethod of the flow
     */
    private static List<MethodCall> findAllServiceMethodCalls(Flow flow) {
        return flow.getController().getMethodCalls().stream()
                .filter(mc -> mc.getCalledFrom().equals(flow.getControllerMethod().getName()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * This method find's the service field affiliated with a methodCall of a flow
     *
     * @param flow the flow
     * @return the field called from this flow's serviceMethodCall
     */
    private static Field findServiceField(Flow flow) {
        return flow.getController().getFields().stream()
                .filter(f -> f.getName().equals(flow.getServiceMethodCall().getObjectName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * This method finds any jClass affiliated with the serviceField of a flow.
     * Due to polymorphism the type is not guaranteed to match one class so all
     * possibilities will be considered.
     * Note: This is a source of approximation -- Runtime types
     *
     * @param flow the flow
     * @return the jClass affiliated with the serviceField
     */
    private static List<JClass> findAllServices(Flow flow) {
        return flow.getModel().getServices().stream()
                .filter(
                        jClass -> jClass.getImplementedTypes().contains(flow.getControllerServiceField().getType()) || jClass.getName().equals(flow.getControllerServiceField().getType()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * This method finds the method affiliated with the serviceMethodCall of a flow
     *
     * @param flow the flow
     * @return the method affiliated with the serviceMethodCall
     */
    private static Method findServiceMethod(Flow flow) {
        return flow.getService().getMethods().stream()
                .filter(
                        method -> method.getName().equals(flow.getServiceMethodCall().getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * This method find's all method calls from the serviceMethod of a flow
     *
     * @param flow the flow
     * @return the list of MethodCalls from the serviceMethod of the flow
     */
    private static List<MethodCall> findAllRepositoryMethodCalls(Flow flow) {
        return flow.getService().getMethodCalls().stream()
                .filter(mc -> mc.getCalledFrom().equals(flow.getServiceMethod().getName()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * This method find's the repository field affiliated with a methodCall of a flow
     *
     * @param flow the flow
     * @return the field called from this flow's repositoryMethodCall
     */
    private static Field findRepositoryField(Flow flow) {
        return flow.getService().getFields().stream()
                .filter(f -> f.getName().equals(flow.getRepositoryMethodCall().getObjectName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * This method finds any jClass affiliated with the repositoryField of a flow.
     * Due to polymorphism the type is not guaranteed to match one class so all
     * possibilities will be considered.
     * Note: This is a source of approximation -- Runtime types
     *
     * @param flow the flow
     * @return the jClass affiliated with the repositoryField
     */
    private static List<JClass> findAllRepositorys(Flow flow) {
        return flow.getModel().getRepositories().stream()
                .filter(
                        jClass -> jClass.getImplementedTypes().contains(flow.getServiceRepositoryField().getType()) || jClass.getName().equals(flow.getServiceRepositoryField().getType()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * This method finds the method affiliated with the repositoryMethodCall of a flow
     *
     * @param flow the flow
     * @return the method affiliated with the repositoryMethodCall
     */
    private static Method findRepositoryMethod(Flow flow) {
        return flow.getRepository().getMethods().stream()
                .filter(
                        method -> method.getName().equals(flow.getRepositoryMethodCall().getName()))
                .findFirst()
                .orElse(null);
    }
}