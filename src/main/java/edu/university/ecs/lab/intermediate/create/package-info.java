/**
 * This package contains the classes responsible for creating the intermediate representation (IR) from remote repositories.
 *
 * <p>The primary class in this package is {@link edu.university.ecs.lab.intermediate.create.IRExtractionRunner}, which serves
 * as the main entry point for initiating the IR extraction process. This process involves using the {@link edu.university.ecs.lab.intermediate.create.services.IRExtractionService}
 * to clone repositories, scan them for REST endpoints and calls, and then write the extracted data to an intermediate representation file.</p>
 */
package edu.university.ecs.lab.intermediate.create;
