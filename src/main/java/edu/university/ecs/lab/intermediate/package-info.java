/**
 * This package contains the classes and sub-packages responsible for the intermediate representation extraction process.
 *
 * <p>The main sub-package within this package is {@code create}, which includes the classes responsible for generating
 * the intermediate representation from the source code. The key classes within the {@code create} package are:
 * </p>
 * <ul>
 *   <li>{@link edu.university.ecs.lab.intermediate.create.IRExtractionRunner}: The main entry point for the intermediate extraction process.</li>
 *   <li>{@link edu.university.ecs.lab.intermediate.create.services.IRExtractionService}: The service class that does a test run and handles the extraction of intermediate representations from remote repositories.</li>
 * </ul>
 *
 * <p>The intermediate extraction process involves cloning remote services, scanning through each local repository to extract REST endpoints and calls,
 * and writing the extracted data into an intermediate representation.</p>
 */
package edu.university.ecs.lab.intermediate;
