package edu.university.ecs.lab.detection.metrics.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MetricResultCalculation {
    private HashMap<String, List<Double>> metrics;

    public MetricResultCalculation() {
        this.metrics = new HashMap<>();
    }

    public void addMetric(String metricName, Double metricValue) {
        if (metrics.containsKey(metricName)) {
            metrics.get(metricName).add(metricValue);
        } else {
            List<Double> metricValues = new ArrayList<>();
            metricValues.add(metricValue);
            metrics.put(metricName, metricValues);
        }
    }

    public HashMap<String, List<Double>> getMetrics() {
        return metrics;
    }

    public double getAverage(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        return metricValues.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    public double getMax(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        return metricValues.stream().max(Double::compare).get();
    }

    public double getMin(String metricName) {
        List<Double> metricValues = metrics.get(metricName);
        if (metricValues.isEmpty()) {
            return 0;
        }
        return metricValues.stream().min(Double::compare).get();
    }

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
