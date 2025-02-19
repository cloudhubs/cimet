package edu.university.ecs.lab.detection_old.metrics.models;


import java.io.IOException;

public interface IServiceDescriptorBuilder extends IInputFile {
    IServiceDescriptor build(String filePath) throws IOException;
}
