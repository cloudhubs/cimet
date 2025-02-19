/**
 * This package contains classes that are services for calculating various metrics related to microservices
 * and service dependency graphs.
 * <p>
 * This package includes:
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.AbstractMetric}:
 *   Contains an Abstract class providing a base for implementing metrics.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.IMetric}:
 *   Contains an interface defining methods for metric calculation.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.LackOfMessageLevelCohesion}:
 *   Contains a service calculating the Lack of Message-Level Cohesion (LMC) metric between microservices operations.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.MetricCalculator}:
 *   Contains a utility class that orchestrates the evaluation of multiple metrics for a given service descriptor.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.MetricResult}:
 *   Represents the result of a single metric calculation.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.MetricResultCalculation}:
 *   Contains the aggregation and calculation overall metric results.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.NumberOfOperations}:
 *   Contains a service calculating the total number of operations within a microservice.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.ServiceInterfaceDataCohesion}:
 *   Contains a service Interface Data Cohesion (SIDC) metric calculation service.
 * - {@link edu.university.ecs.lab.detection_old.metrics.services.StrictServiceImplementationCohesion}:
 *   Contains the Strict Service Implementation Cohesion (SSIC) metric calculation service.
 * </p>
 */
package edu.university.ecs.lab.detection_old.metrics.services;
