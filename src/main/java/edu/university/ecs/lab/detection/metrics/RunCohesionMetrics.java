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

public class RunCohesionMetrics {

    public static void main(String[] args) {
        calculateCohesionMetrics("./output/OldIR.json");
    }

    public static MetricResultCalculation calculateCohesionMetrics(String IRPath) {
        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON(IRPath, MicroserviceSystem.class);


        MetricResultCalculation metricResultCalculation = new MetricResultCalculation();


        for (Microservice microservice : microserviceSystem.getMicroservices()) {

            IServiceDescriptor serviceDescriptor = new ServiceDescriptor();
//                JSONObject microservice = microservices.getJSONObject(i);
//                String serviceName = microservice.getString("name");
//                JSONArray controllers = microservice.getJSONArray("controllers");

            serviceDescriptor.setServiceName(microservice.getName());

            for (JClass controller : microservice.getControllers()) {
//                    JSONObject controller = controllers.getJSONObject(j);
//                    JSONArray methods = controller.getJSONArray("methods");

                List<Operation> operations = new ArrayList<>();

                for (Method method : controller.getMethods()) {
//                        JSONObject method = methods.getJSONObject(k);
                    Operation operation = new Operation();
                    String operationName = microservice.getName() + "::" + method.getName();
                    operation.setResponseType(method.getReturnType());
                    operation.setName(operationName);

//                        JSONArray parameters = method.getJSONArray("parameters");
                    List<Parameter> paramList = new ArrayList<>();
                    for (Field field : method.getParameters()) {
//                            JSONObject parameter = parameters.getJSONObject(l);
                        Parameter param = new Parameter();
                        param.setName(field.getName());
                        param.setType(field.getType());
                        paramList.add(param);
                    }
                    operation.setParamList(paramList);

                    List<String> usingTypes = new ArrayList<>();
                    // Assume annotations can imply using types
//                        JSONArray annotations = method.getJSONArray("annotations");
                    for (Annotation annotation : method.getAnnotations()) {
//                            JSONObject annotation = annotations.getJSONObject(m);
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

