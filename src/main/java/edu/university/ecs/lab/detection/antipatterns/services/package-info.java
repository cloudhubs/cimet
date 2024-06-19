/**
 * Provides services for detecting and analyzing various anti-patterns in microservices architecture.
 * These services encapsulate logic for identifying specific issues within microservice systems and
 * generating reports or performing corrective actions.
 * <p>
 * Services:
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.CyclicDependencyService}: Service for detecting
 *   cyclic dependencies within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.GreedyService}: Service for identifying and managing
 *   microservices identified as greedy based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.HubLikeService}: Service for identifying and managing
 *   microservices identified as hub-like based on REST call thresholds.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.ServiceChainService}: Service for detecting and managing
 *   service chains within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.WrongCutsService}: Service for identifying and reporting
 *   clusters of services that are incorrectly interconnected within a microservice network graph.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.WobblyServiceInteractionService}: Service for detecting
 *   wobbly service interactions within a microservice system based on specific annotations.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.NoApiGateway}: Service for checking the presence of an
 *   API gateway configuration in a YAML file.
 * - {@link edu.university.ecs.lab.detection.antipatterns.services.NoHealthcheck}: Service for checking the presence of
 *   health check configurations in a YAML file.
 * </p>
 */
package edu.university.ecs.lab.detection.antipatterns.services;
