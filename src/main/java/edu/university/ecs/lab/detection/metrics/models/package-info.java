/**
 * This package contains classes that represent models used for calculating various metrics related to microservices
 * and service dependency graphs.
 * <p>
 * This package includes:
 *  - {@link edu.university.ecs.lab.detection.metrics.models.ConnectedComponentsModularity}:
 *    Contains the implementation of the modularity metric of partitioning a graph into strongly connected components.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.DegreeCoupling}:
 *    Contains the calculation of degree-related Coupling metrics.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.IInputFile}:
 *    Represents an input file of different formats and creates a Service Descriptor object.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.IServiceDescriptor}:
 *    Represents a service descriptor interface defining methods for service name, version, operations, and their types.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.IServiceDescriptorBuilder}: Provides a method to build a service descriptor from a file path.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.Operation}:
 *    Represents an operation of a microservice with parameters, using types, response type, and path.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.Parameter}:
 *    Represents a parameter of a method or operation.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.ServiceDescriptor}:
 *  - {@link edu.university.ecs.lab.detection.metrics.models.Statements}: Enumerates various statements or types used within the metrics calculation context.
 *    Represents a service descriptor with service name, operations, and version.
 *  - {@link edu.university.ecs.lab.detection.metrics.models.StructuralCoupling}: Contains the implementation of the Structural Coupling Metric.
 * </p>
 */
package edu.university.ecs.lab.detection.metrics.models;
