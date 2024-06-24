/**
 * Provides classes and services for detecting various anti-patterns in microservices architecture.
 * Includes detection of cyclic dependencies, greedy microservices, hub-like microservices, wrong cuts,
 * service chains, wobbly service interactions, and absence of API gateway and health checks.
 * <p>
 * Classes:
 * - {@link edu.university.ecs.lab.detection.antipatterns.AntipatternDetection}: Main class for running
 *   anti-pattern detection routines using configuration and services.
 * <p>
 * Models Package:
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency}: Represents cyclic dependencies
 *   detected within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.GreedyMicroservice}: Represents microservices identified
 *   as greedy based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice}: Represents microservices identified
 *   as hub-like based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.NoApiGateway}: Represents the absence of an API Gateway,
 *   indicating potential issues with centralized routing and access control.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.NoHealthcheck}: Represents the absence of health check
 *   mechanisms, which are crucial for monitoring and maintaining the health of microservices.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.ServiceChain}: Represents a chain of services within a
 *   microservice network graph, potentially introducing latency and complexity.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.WrongCuts}: Represents clusters of services that are
 *   incorrectly segmented, leading to inefficiencies and increased coupling within the microservice network.
 * - {@link edu.university.ecs.lab.detection.antipatterns.models.WobblyServiceInteraction}: Represents service interactions
 *   characterized by unstable or inconsistent communication patterns within microservice classes and methods.
 * </p>
 * <p>
 * Services Package:
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.CyclicDependencyService}: Service for detecting
 *   cyclic dependencies in a network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.GreedyService}: Service for identifying and managing
 *   microservices identified as greedy based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.HubLikeService}: Service for identifying and managing
 *   microservices identified as hub-like based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.ServiceChainService}: Service for detecting and
 *   managing service chains in a network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.WrongCutsService}: Service for identifying and
 *   reporting clusters of services that are incorrectly interconnected in a network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.WobblyServiceInteractionService}: Service for detecting
 *   wobbly service interactions in a microservice system based on specific annotations.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.NoApiGatewayService}: Service for checking the presence of an
 *   API gateway configuration in a YAML file.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.NoHealthcheckService}: Service for checking the presence of
 *   health check configurations in a YAML file.
 * </p>
 */
package edu.university.ecs.lab.detection.antipatterns;
