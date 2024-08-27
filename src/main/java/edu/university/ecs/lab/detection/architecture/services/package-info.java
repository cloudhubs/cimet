/**
 * Provides services for detecting architectural rules within microservice systems.
 * Includes utilities for reading system changes and microservice snapshots,
 * and utilizes specific architectural rule classes for detection.
 * 
 * <p>The main class in this package is {@link edu.university.ecs.lab.detection.architecture.services.ARDetectionService},
 * which orchestrates the detection process by scanning {@link edu.university.ecs.lab.delta.models.Delta} changes
 * and comparing {@link edu.university.ecs.lab.common.models.ir.MicroserviceSystem} instances to identify instances
 * of architectural rules.</p>
 */
package edu.university.ecs.lab.detection.architecture.services;
