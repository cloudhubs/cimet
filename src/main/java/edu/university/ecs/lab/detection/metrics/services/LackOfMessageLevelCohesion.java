package edu.university.ecs.lab.detection.metrics.services;

import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;
import edu.university.ecs.lab.detection.metrics.models.Operation;
import edu.university.ecs.lab.detection.metrics.models.Parameter;
import edu.university.ecs.lab.detection.metrics.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metric Service Class to determine Lack of Message Level Cohesion (LMC) metric between microservice operations
 * 
 * LMC = sum(1 - ((iDS + oDS) / 2)) / numberOfPairs
 */
public class LackOfMessageLevelCohesion extends AbstractMetric {

    public LackOfMessageLevelCohesion() {
        this.setMetricName("LackOfMessageLevelCohesion");
    }

    /**
     * Evaluate lack of message level cohesion
     */
    @Override
    public void evaluate() {
        IServiceDescriptor serviceDescriptor = this.getServiceDescriptor();

        MetricResult metricResult = new MetricResult();
        metricResult.setMetricName(this.getMetricName());
        metricResult.setServiceName(serviceDescriptor.getServiceName());
        metricResult.setVersion(serviceDescriptor.getServiceVersion());
        metricResult.setMetricValue(0.0);

        this.setResult(metricResult);

        if (serviceDescriptor == null || serviceDescriptor.getServiceOperations() == null) {
            // do nothing
        } else if (serviceDescriptor.getServiceOperations().size() == 0) {
            // do nothing
            this.getResult().setMetricValue(1.0);
        } else if (serviceDescriptor.getServiceOperations().size() == 1) {
            // do nothing
            this.getResult().setMetricValue(1.0);
        } else {
            List<Operation> operations = serviceDescriptor.getServiceOperations();
            ArrayList<ArrayList<Operation>> operationPairs = Utils.pairs(operations);

            int numberOfPairs = operationPairs.size();

            // Create a list of average iDS/oDS of operation pairs
            ArrayList<Double> operationSimilarityList = new ArrayList<>();
            for (ArrayList<Operation> pair : operationPairs) {
                Operation firstOperation = pair.get(0);
                Operation secondOperation = pair.get(1);

                double iDS = inputDataSimilarity(firstOperation, secondOperation);
                double oDS = outputDataSimilarity(firstOperation, secondOperation);

                double operationSimilarity = ( iDS + oDS) / 2;

                operationSimilarityList.add(operationSimilarity);
            }

            // Sum of complements of avg iDS/oDS
            double acc = 0.0;
            for (Double similarity : operationSimilarityList) {
                acc = acc + (1 - similarity);
            }

            // Return sum divided by number of pairs
            this.getResult().setMetricValue(acc / numberOfPairs);
        }
    }

    /**
     * Measure the data input similarity of two given operations
     * 
     * @param firstOperation first operation to compare data input
     * @param secondOperation second operation to compare data input
     * @return 1.0 if there are no operation input parameters, otherwise double measure of data input similarity
     */
    public Double inputDataSimilarity(Operation firstOperation, Operation secondOperation) {
        HashSet<String> unionOfProperties = new HashSet<>();

        // Get parameter names from each operation
        List<String> firstOperationParameterNames = Parameter.getParameterNames(firstOperation.getParamList());
        List<String> secondOperationParameterNames = Parameter.getParameterNames(secondOperation.getParamList());

        // Join properties of both operations and get total number of properties
        unionOfProperties.addAll(firstOperationParameterNames);
        unionOfProperties.addAll(secondOperationParameterNames);

        int sizeOfUnionOfProperties = unionOfProperties.size();

        // Get common parameters/properties
        List<String> commonProperties = firstOperationParameterNames.stream()
                .filter(secondOperationParameterNames::contains).collect(Collectors.toList());

        // Number of common properties
        int cps = commonProperties.size();

        if (sizeOfUnionOfProperties == 0.0 && cps == 0) {
            return 1.0;
        }

        // Input data similarity is measured as the ratio of common properties to total sum of properties
        double inputDataSimilarity = cps / (sizeOfUnionOfProperties * 1.0);

        return inputDataSimilarity;
    }

    /**
     * Measure the data output similarity of two given operations
     * 
     * @param firstOperation first operation to compare data input
     * @param secondOperation second operation to compare data input
     * @return 1.0 if the operations share a response type, otherwise 0.0
     */
    public Double outputDataSimilarity(Operation firstOperation, Operation secondOperation) {
        HashSet<String> unionOfProperties = new HashSet<>();
        unionOfProperties.add(firstOperation.getResponseType());
        unionOfProperties.add(secondOperation.getResponseType());

        return unionOfProperties.size() == 1 ? 1.0 : 0.0;
    }
}
