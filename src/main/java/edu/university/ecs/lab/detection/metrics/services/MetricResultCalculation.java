package edu.university.ecs.lab.detection.metrics.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Calculation and aggregation class using metric result values
 */
public class MetricResultCalculation {
    private HashMap<String, List<Double>> metrics;

    public MetricResultCalculation() {
        this.metrics = new HashMap<>();
    }

    /**
     * Adds/maps metric values to a given metric name
     * 
     * @param metricName name of the given metric
     * @param metricValue value of the given metric
     */
    public void addMetric(String metricName, Double metricValue) {
        if (metrics.containsKey(metricName)) {
            metrics.get(metricName).add(metricValue);
        } else {
            List<Double> metricValues = new ArrayList<>();
            metricValues.add(metricValue);
            metrics.put(metricName, metricValues);
        }
    }

    /**
     * Get the list of metrics
     * 
     * @return hasmap list of metrics
     */
    public HashMap<String, List<Double>> getMetrics() {
        return metrics;
    }

    /**
     * Calculate the average value of a given metric
     * 
     * @param metricName name of the given metric
     * @return average of all values listed under given metric name
     */
    public double getAverage(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        return metricValues.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    /**
     * Find the maximum value of a given metric
     * 
     * @param metricName name of the given metric
     * @return maximum of all values listed under given metric name
     */
    public double getMax(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        return metricValues.stream().max(Double::compare).get();
    }

    /**
     * Find the minimum value of a given metric
     * 
     * @param metricName name of the given metric
     * @return minimum of all values listed under given metric name
     */
    public double getMin(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        return metricValues.stream().min(Double::compare).get();
    }

    /**
     * Calculate the standard deviation of a given metric
     * 
     * @param metricName name of the given metric
     * @return standard deviation of all values listed under given metric name
     */
    public double getStdDev(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        double mean = getAverage(metricName);
        double sum = metricValues.stream().mapToDouble(value -> Math.pow(value - mean, 2)).sum();
        return Math.sqrt(sum / metricValues.size());
    }

}
