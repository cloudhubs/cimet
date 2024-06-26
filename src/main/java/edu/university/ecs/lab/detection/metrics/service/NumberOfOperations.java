package edu.university.ecs.lab.detection.metrics.service;

import edu.university.ecs.lab.detection.metrics.model.IServiceDescriptor;

public class NumberOfOperations extends AbstractMetric {

    public NumberOfOperations() {
        this.setMetricName("NumberOfOperations");
    }

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
