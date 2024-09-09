package edu.university.ecs.lab.detection.metrics.services;

import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for calculation of multiple metrics for a given service descriptor
 */
public class MetricCalculator {
    //    Add here all metrics instances
    private List<IMetric> getMetrics() {
        return Arrays.asList(
                new ServiceInterfaceDataCohesion(),
                new StrictServiceImplementationCohesion(),
                new LackOfMessageLevelCohesion(),
                new NumberOfOperations()
        );
    }

    /**
     * Assess and get results of each metric instance
     * 
     * @param serviceDescriptor descriptor object of the given service
     * @return list of metric results
     */
    public List<MetricResult> assess(IServiceDescriptor serviceDescriptor) {
        List<IMetric> metrics = getMetrics();

        if (metrics.size() == 0) {
            return Arrays.asList();
        }

        List<MetricResult> metricResults = metrics.stream().map(metric -> {
            metric.setServiceDescriptor(serviceDescriptor);
            metric.evaluate();
            return metric.getResult();
        }).collect(Collectors.toList());

        return metricResults;
    }
}
