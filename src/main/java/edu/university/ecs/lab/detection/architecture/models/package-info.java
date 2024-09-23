/**
 * Provides classes and enums for representing architectural rules within the microservice system.
 * This package is part of the {@link edu.university.ecs.lab.detection.architecture} package, which focuses on detecting architectural rules and related information in a microservice system.
 *
 * <p>This package includes:
 *   - {@link edu.university.ecs.lab.detection.architecture.models.enums}: Contains enumerations used within the architectural models, such as confidence levels.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AbstractAR}: Provides a template for all architectural rules, including methods to get the name, description, weight, commit IDs, and type of the rule, and to convert the rule to a JSON object.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR1}: Represents the rule for detecting floating calls due to endpoint removal within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR3}: Represents the rule for detecting floating calls due to invalid call creation within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR4}: Represents the rule for detecting floating endpoints due to last call removal within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR6}: Represents the rule for detecting affected endpoints due to business logic updates within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR7}: Represents the rule for detecting affected endpoints due to data access logic updates within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR20}: Represents the rule for identifying hub-like services within the microservice architecture, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR21}: Represents the rule for detecting clusters of wrongly interconnected services (wrongcuts) within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR22}: Represents the rule for detecting inconsistent modifications of entities across services within the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR23}: Represents the rule for detecting the absence of API gateway configuration in the microservice system, including methods for scanning and detecting such instances.
 *   - {@link edu.university.ecs.lab.detection.architecture.models.AR24}: Represents the rule for detecting the absence of health check configurations in the microservice system, including methods for scanning and detecting such instances.
 * </p>
 */
package edu.university.ecs.lab.detection.architecture.models;
