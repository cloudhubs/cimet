/**
 * This package contains classes and sub-packages related to the extraction of delta changes
 * between commits in a microservice system repository.
 *
 * <p>The main components include:</p>
 *   - The {@link edu.university.ecs.lab.delta.models} package, which holds the data models representing individual
 *   and overall changes between commits.
 *   - The {@link edu.university.ecs.lab.delta.models.enums} package, which defines enumerations used within the
 *   data models, such as {@link edu.university.ecs.lab.delta.models.enums.ChangeType}.
 *   - The {@link edu.university.ecs.lab.delta.services} package, which provides services for extracting and
 *   processing differences between commits, such as the {@link edu.university.ecs.lab.delta.services.DeltaExtractionService}.
 *
 * <p>The package also includes a runner class, {@link edu.university.ecs.lab.delta.DeltaExtractionRunner}, for executing
 * a test delta process.</p>
 */
package edu.university.ecs.lab.delta;
