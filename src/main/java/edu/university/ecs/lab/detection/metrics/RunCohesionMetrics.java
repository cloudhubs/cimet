package edu.university.ecs.lab.detection.metrics;

import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;
import edu.university.ecs.lab.detection.metrics.models.Operation;
import edu.university.ecs.lab.detection.metrics.models.Parameter;
import edu.university.ecs.lab.detection.metrics.models.ServiceDescriptor;
import edu.university.ecs.lab.detection.metrics.services.MetricCalculator;
import edu.university.ecs.lab.detection.metrics.services.MetricResult;
import edu.university.ecs.lab.detection.metrics.services.MetricResultCalculation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunCohesionMetrics {

    public static void main(String[] args) {
        calculateCohesionMetrics("./output/OldIR.json");
    }

    public static MetricResultCalculation calculateCohesionMetrics(String IRPath) {
        try (FileInputStream inputStream = new FileInputStream(IRPath)) {

            JSONTokener tokenizer = new JSONTokener(inputStream);
            JSONObject root = new JSONObject(tokenizer);

            System.out.println(root.getString("name"));

            JSONArray microservices = root.getJSONArray("microservices");

            MetricResultCalculation metricResultCalculation = new MetricResultCalculation();


            for (int i = 0; i < microservices.length(); i++) {

                IServiceDescriptor serviceDescriptor = new ServiceDescriptor();
                JSONObject microservice = microservices.getJSONObject(i);
                String serviceName = microservice.getString("name");
                JSONArray controllers = microservice.getJSONArray("controllers");

                serviceDescriptor.setServiceName(serviceName);

                for (int j = 0; j < controllers.length(); j++) {
                    JSONObject controller = controllers.getJSONObject(j);
                    JSONArray methods = controller.getJSONArray("methods");

                    List<Operation> operations = new ArrayList<>();

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        Operation operation = new Operation();
                        String operationName = microservice.getString("name") + "::" + method.getString("name");
                        operation.setResponseType(method.getString("returnType"));
                        operation.setName(operationName);

                        JSONArray parameters = method.getJSONArray("parameters");
                        List<Parameter> paramList = new ArrayList<>();
                        for (int l = 0; l < parameters.length(); l++) {
                            JSONObject parameter = parameters.getJSONObject(l);
                            Parameter param = new Parameter();
                            param.setName(parameter.getString("name"));
                            param.setType(parameter.getString("type"));
                            paramList.add(param);
                        }
                        operation.setParamList(paramList);

                        List<String> usingTypes = new ArrayList<>();
                        // Assume annotations can imply using types
                        JSONArray annotations = method.getJSONArray("annotations");
                        for (int m = 0; m < annotations.length(); m++) {
                            JSONObject annotation = annotations.getJSONObject(m);
                            usingTypes.add(annotation.getString("name") + " - " + annotation.getString("contents"));
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

            System.out.println(metricResultCalculation);

            return metricResultCalculation;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

