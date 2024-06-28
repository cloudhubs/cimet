package edu.university.ecs.lab.detection.metrics.services;

import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;
import edu.university.ecs.lab.detection.metrics.models.Operation;
import edu.university.ecs.lab.detection.metrics.models.Parameter;
import edu.university.ecs.lab.detection.metrics.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The measurement procedure consists of comparing the sets of parameter types for each
 * service operation so∈ SO (sis) in a pair-wise manner, and then placing the operations
 * with common parameter types into a set of Common operations. The cardinality of this
 * set is then divided by a total number of discrete parameter types for this service.
 * <p>
 * SIDC (s) = |Common(Param(so ∈ SO(sis )| / totalParamTypes
 */
public class ServiceInterfaceDataCohesion extends AbstractMetric {

    public ServiceInterfaceDataCohesion() {
        this.setMetricName("ServiceInterfaceDataCohesion");
    }

    @Override
    /**
     * TODO: refactor! It is a mess
     */
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
            this.getResult().setMetricValue(1.0);
        } else if (serviceDescriptor.getServiceOperations().size() == 1) {
            this.getResult().setMetricValue(1.0);
        } else {
            // set of all operations type
            long totalOfParamsType = serviceDescriptor.getServiceOperations()
                    .stream()
                    .map(o -> o.getParamList())
                    .map(o -> Parameter.getParameterTypes(o))
                    .filter(Objects::nonNull)
                    .flatMap(types -> types.stream())
                    .distinct()
                    .count();

            ArrayList<ArrayList<Operation>> pairsOfOperations = Utils.pairs(serviceDescriptor.getServiceOperations());

            int intersectTypesSize = pairsOfOperations.stream().map(pair -> {
                Operation operation1 = pair.get(0);
                Operation operation2 = pair.get(1);

                return commonParameterTypes(operation1, operation2);
            }).flatMap(types -> types.stream()).collect(Collectors.toSet()).size();

            this.getResult().setMetricValue(intersectTypesSize / (totalOfParamsType * 1.0));
        }
    }

    private List<String> commonParameterTypes(Operation operation1, Operation operation2) {
        List<String> typesIntoFirstOperation = Parameter.getParameterTypes(operation1.getParamList());
        List<String> typesIntoSecondOperation = Parameter.getParameterTypes(operation2.getParamList());

        return typesIntoFirstOperation.stream()
                .filter(typesIntoSecondOperation::contains).collect(Collectors.toList());
    }
}
