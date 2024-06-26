package edu.university.ecs.lab.detection.metrics.model;


import java.io.IOException;

public interface IServiceDescriptorBuilder extends IInputFile {
    IServiceDescriptor build(String filePath) throws IOException;
}
