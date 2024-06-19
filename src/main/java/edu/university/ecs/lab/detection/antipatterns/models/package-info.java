/**
 * Contains model classes representing various entities related to microservices and anti-pattern detection.
 * These models are used to represent different aspects of microservice architecture and anti-patterns.
 * <p>
 * Models:
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency}: Represents cyclic dependencies
 *   detected within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.GreedyMicroservice}: Represents microservices identified
 *   as greedy based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice}: Represents microservices identified
 *   as hub-like based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.ServiceChain}: Represents a chain of services within a
 *   microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.WrongCuts}: Represents clusters of services that are
 *   incorrectly interconnected within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.WobblyServiceInteraction}: Represents service interactions
 *   characterized by specific annotations within microservice classes and methods.
 * </p>
 * These models are utilized across various services and components within the anti-pattern detection framework to
 * analyze and report issues related to microservices architecture.
 */
package edu.university.ecs.lab.detection.antipatterns.models;
