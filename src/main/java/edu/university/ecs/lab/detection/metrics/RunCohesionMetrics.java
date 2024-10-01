package edu.university.ecs.lab.detection.metrics;

import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;
import edu.university.ecs.lab.detection.metrics.models.Operation;
import edu.university.ecs.lab.detection.metrics.models.Parameter;
import edu.university.ecs.lab.detection.metrics.models.ServiceDescriptor;
import edu.university.ecs.lab.detection.metrics.services.MetricCalculator;
import edu.university.ecs.lab.detection.metrics.services.MetricResult;
import edu.university.ecs.lab.detection.metrics.services.MetricResultCalculation;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates cohesion metrics for a microservice system based on its intermediate representation (IR).
 */
public class RunCohesionMetrics {

    public static void main(String[] args) {
        calculateCohesionMetrics("./output/OldIR.json");
    }

    /**
     * Calculate cohesion metrics based on microservice IR
     * 
     * @param IRPath path to intermediate representation JSON
     * @return metric result calculation object
     */
    public static MetricResultCalculation calculateCohesionMetrics(String IRPath) {
        
        // Create microservice system object
        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON(IRPath, MicroserviceSystem.class);

        MetricResultCalculation metricResultCalculation = new MetricResultCalculation();

        for (Microservice microservice : microserviceSystem.getMicroservices()) {

            IServiceDescriptor serviceDescriptor = new ServiceDescriptor();

            serviceDescriptor.setServiceName(microservice.getName());

            for (JClass controller : microservice.getControllers()) {

                List<Operation> operations = new ArrayList<>();

                for (Method method : controller.getMethods()) {
                    Operation operation = new Operation();
                    String operationName = microservice.getName() + "::" + method.getName();
                    operation.setResponseType(method.getReturnType());
                    operation.setName(operationName);

                    List<String> paramList = new ArrayList<>();
                    for (edu.university.ecs.lab.common.models.ir.Parameter field : method.getParameters()) {
//                        Parameter param = new Parameter();
//                        param.setName(field.getName());
//                        param.setType(field.getType());
                        paramList.add(field.getType());
                    }
                    operation.setParamList(paramList);

                    List<String> usingTypes = new ArrayList<>();
                    for (Annotation annotation : method.getAnnotations()) {
                        usingTypes.add(annotation.getName() + " - " + annotation.getContents());
                    }
                    operation.setUsingTypesList(usingTypes);

                    operations.add(operation);
                }

                serviceDescriptor.setServiceOperations(operations);
            }

            List<MetricResult> metricResults = new MetricCalculator().assess(serviceDescriptor);

            for (MetricResult metricResult : metricResults) {
                metricResultCalculation.addMetric(metricResult.getMetricName(), metricResult.getMetricValue());
            }

        }


        return metricResultCalculation;


    }

}

