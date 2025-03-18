package edu.university.ecs.lab.detection.reporting;

import edu.university.ecs.lab.detection.scanning.common.AbstractScanner;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * A pipeline class for running analytics on a continuous flow of data.
 * @param <T> The data type
 */
@NoArgsConstructor
public class Pipeline<T> {
    /**
     * The data source for the pipeline.
     */
    private Iterable<T> dataSource;

    /**
     * The scanners registered to this pipeline.
     */
    private List<AbstractScanner> scanners;

    /**
     * The reporters registered to this pipeline.
     */
    private List<AbstractReporter> reporters;

    /**
     * Registers new scanners with this pipeline.
     * @param scanners The scanner(s) to register.
     */
    public void registerScanners(AbstractScanner... scanners) {
        this.scanners.addAll(Arrays.asList(scanners));
    }

    /**
     * Removes registered scanners from this pipeline.
     * @param scanners The scanner(s) to remove.
     */
    public void deregisterScanners(AbstractScanner... scanners) {
        this.scanners.removeAll(Arrays.asList(scanners));
    }

    /**
     * Registers new reporters with this pipeline.
     * @param reporters The reporter(s) to register.
     */
    public void registerReporters(AbstractReporter... reporters) {
        this.reporters.addAll(Arrays.asList(reporters));
    }

    /**
     * Removes registered reporters from this pipeline.
     * @param reporters The reporter(s) to remove.
     */
    public void deregisterReporters(AbstractReporter... reporters) {
        this.reporters.removeAll(Arrays.asList(reporters));
    }

    public void runScanners() {
        this.scanners.forEach(AbstractScanner::scan);
    }

    public void runReporters() {
        this.reporters.forEach(AbstractReporter::report);
    }
}
