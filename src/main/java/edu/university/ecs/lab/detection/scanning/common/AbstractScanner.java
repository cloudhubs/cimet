package edu.university.ecs.lab.detection.scanning.common;

import edu.university.ecs.lab.detection.models.results.DetectionResult;
import edu.university.ecs.lab.detection.strategies.ScanStrategy;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractScanner implements ScanStrategy {
    protected List<DetectionResult> results;

    protected String name;

    public abstract void setStrategy();

    public abstract boolean scan();

    public abstract void report();

    public void addResults(DetectionResult... results) {
        this.results.addAll(Arrays.asList(results));
    }
}
