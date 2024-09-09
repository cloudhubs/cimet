package edu.university.ecs.lab.detection.metrics.services;

import edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor;

/**
 * Abstract metric class template for all metrics
 */
public abstract class AbstractMetric implements IMetric {
    
    /**
     * Name of the metric
     */
    private String metricName;

    /**
     * Description of the service
     */
    private IServiceDescriptor serviceDescriptor;
    
    /**
     * Results of the given metric
     */
    private MetricResult result;

    public AbstractMetric() {
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public IServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(IServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public MetricResult getResult() {
        return result;
    }

    public void setResult(MetricResult result) {
        this.result = result;
    }
}
