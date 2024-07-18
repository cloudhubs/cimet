/**
 * This package contains classes and utilities for calculating various metrics related to microservices
 * and service dependency graphs.
 * <p>
 * This package include:
 *  - {@link edu.university.ecs.lab.detection.metrics.MetricCalculation}: Calculates structural and degree coupling metrics,
 *   and modularity metrics for a microservice system based on service dependency graph (SDG).
 *  - {@link edu.university.ecs.lab.detection.metrics.RunCohesionMetrics}: Calculates cohesion metrics for a microservice system
 *   based on its intermediate representation (IR).
 *  - {@link edu.university.ecs.lab.detection.metrics.models}: Contains models used in metric calculations.
 *  - {@link edu.university.ecs.lab.detection.metrics.services}: Contains services for calculating and aggregating metric results.
 *  - {@link edu.university.ecs.lab.detection.metrics.utils}: Contains utility classes for common operations related to metric calculations. 
 * </p>
 * The main purpose of this package is to provide tools for assessing the architecture and design quality of microservice systems
 * through various quantitative metrics.
 */
package edu.university.ecs.lab.detection.metrics;
