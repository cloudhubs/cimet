package edu.university.ecs.lab.detection.scanning.antipatterns;

import edu.university.ecs.lab.detection.scanning.common.AbstractScanner;

public abstract class AbstractAntipatternScanner extends AbstractScanner {
    public boolean scan() {
        return false;
    }

    public boolean filter() {
        return false;
    }

    public void report() {}
}
