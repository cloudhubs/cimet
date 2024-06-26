package edu.university.ecs.lab.detection.metrics.model;

import java.util.List;
import java.util.stream.Collectors;

public class Parameter {
    private String type;
    private String name;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<String> getParameterNames(List<Parameter> parameters) {
        return parameters.stream().map(parameter -> parameter.getName()).collect(Collectors.toList());
    }
    public static List<String> getParameterTypes(List<Parameter> parameters) {
        return parameters.stream().map(parameter -> parameter.getType()).collect(Collectors.toList());
    }
}
