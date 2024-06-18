/**
 * Provides classes and sub-packages that represent various components of a microservice system
 * and facilitate configuration of these representations in JSON format.
 * <p>
 * This package includes:
 * - 
 *   - {@link edu.university.ecs.lab.common.models.enums}: Enumerations used for categorizing different components, such as Class Roles, HTTP Methods, etc.
 *   - {@link edu.university.ecs.lab.common.models.serialization}: Serialization and deserialization utilities for converting Java objects to JSON and vice versa
 * - Other model classes representing key elements of the microservice system:
 *   - {@link edu.university.ecs.lab.common.models.Annotation}: Represents annotations within classes.
 *   - {@link edu.university.ecs.lab.common.models.Edge}: Represents an edge in a network graph schema
 *     modeling microservice connections.
 *   - {@link edu.university.ecs.lab.common.models.Endpoint}: Represents an endpoint exposed by a microservice.
 *   - {@link edu.university.ecs.lab.common.models.Field}: Represents fields within classes.
 *   - {@link edu.university.ecs.lab.common.models.JClass}: Represents a Java class within a microservice.
 *   - {@link edu.university.ecs.lab.common.models.Method}: Represents a method within classes.
 *   - {@link edu.university.ecs.lab.common.models.MethodCall}: Represents a method call within microservices.
 *   - {@link edu.university.ecs.lab.common.models.Microservice}: Represents a microservice within the system,
 *     including its components like controllers, services, etc.
 *   - {@link edu.university.ecs.lab.common.models.MicroserviceSystem}: Represents a microservice system and all its components, 
 *     including the name of the system, the set of microservices, etc.
 *   - {@link edu.university.ecs.lab.common.models.NetworkGraph}: Represents the microservice system in a Static SDG schema (nodes and edges)
 *   - {@link edu.university.ecs.lab.common.models.RestCall}: Represents an a call to an endpoing mapping and exists at the service level
 *    
 */
package edu.university.ecs.lab.common.models;