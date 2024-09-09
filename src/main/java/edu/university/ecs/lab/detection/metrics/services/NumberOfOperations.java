package edu.university.ecs.lab.detection.metrics.services;

import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;

/**
 * Metric Service Class to determine the Number of Operations in a microservice
 */
public class NumberOfOperations extends AbstractMetric {

    public NumberOfOperations() {
        this.setMetricName("NumberOfOperations");
    }

    /**
     * Find the nimber of operations in a given service
     */
    @Override
    public void evaluate() {
        IServiceDescriptor serviceDescriptor = this.getServiceDescriptor();

        MetricResult metricResult = new MetricResult();
        metricResult.setMetricName(this.getMetricName());
        metricResult.setServiceName(serviceDescriptor.getServiceName());
        metricResult.setVersion(serviceDescriptor.getServiceVersion());

        metricResult.setMetricValue(serviceDescriptor.getServiceOperations().size() / 1.0);

        this.setResult(metricResult);
    }
}
