/**
 * Contains model classes representing various entities related to microservices and anti-pattern detection.
 * These models are used to represent different aspects of microservice architecture and anti-patterns.
 * <p>
 * Models:
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.CyclicDependency}: Represents cyclic dependencies
 *   detected within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.GreedyMicroservice}: Represents microservices identified
 *   as greedy based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.HubLikeMicroservice}: Represents microservices identified
 *   as hub-like based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.NoApiGateway}: Represents the absence of an API Gateway,
 *   indicating potential issues with centralized routing and access control.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.NoHealthcheck}: Represents the absence of health check
 *   mechanisms, which are crucial for monitoring and maintaining the health of microservices.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.ServiceChain}: Represents a chain of services within a
 *   microservice network graph, potentially introducing latency and complexity.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.WrongCuts}: Represents clusters of services that are
 *   incorrectly segmented, leading to inefficiencies and increased coupling within the microservice network.
 * - {@link edu.university.ecs.lab.detection.models.results.antipatterns.WobblyServiceInteraction}: Represents service interactions
 *   characterized by unstable or inconsistent communication patterns within microservice classes and methods.
 * </p>
 * These models are utilized across various services and components within the anti-pattern detection framework to
 * analyze and report issues related to microservices architecture.
 */
package edu.university.ecs.lab.detection.models.results.antipatterns;
