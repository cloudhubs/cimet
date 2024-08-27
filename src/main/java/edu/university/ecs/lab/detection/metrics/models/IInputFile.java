package edu.university.ecs.lab.detection.metrics.models;

import java.io.IOException;

/**
 * Input file of different formats and creates a Service Descriptor object.
 */
public interface IInputFile {
    IServiceDescriptor build(String filePath) throws IOException;
}
