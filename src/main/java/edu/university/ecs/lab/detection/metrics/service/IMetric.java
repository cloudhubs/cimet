/**
 * 
 */
package edu.university.ecs.lab.detection.metrics.service;

import edu.university.ecs.lab.detection.metrics.model.IServiceDescriptor;

/**
 * @author Mateus Gabi Moreira
 * @version 1.0.0
 */
public interface IMetric {
	public void evaluate();
	public String getMetricName();
	public void setMetricName(String metricName);
	public IServiceDescriptor getServiceDescriptor();
	public void setServiceDescriptor(IServiceDescriptor serviceDescriptor);
	public MetricResult getResult();
	public void setResult(MetricResult result);
}
