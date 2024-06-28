package edu.university.ecs.lab.detection.metrics.services;

import org.json.JSONObject;

public class MetricResult {
    private String serviceName;
    private String version;
    private String metricName;
    private Double metricValue;

    public MetricResult() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public String toString() {
//        return "{" +
//                "serviceName:'" + serviceName + '\'' +
//                ", version='" + version + '\'' +
//                ", metricName='" + metricName + '\'' +
//                ", metricValue=" + metricValue +
//                '}';

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("serviceName", serviceName);
        jsonObject.put("version", version);
        jsonObject.put("metricName", metricName);
        jsonObject.put("metricValue", metricValue);
        return jsonObject.toString();
    }
}
