package edu.university.ecs.lab.intermediate.create;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

import java.io.File;


/**
 * {@link IRExtractionRunner} is the main entry point for the intermediate extraction process, relying on
 * {@link IRExtractionService}.
 *
 * <p>The IR extraction process is responsible for cloning remote services, scanning through each
 * local repo and extracting rest endpoints/calls, and writing each service and endpoints to
 * intermediate representation.
 *
 * <p>
 */
public class IRExtractionRunner {

  /**
   * Intermediate extraction runner, generates IR from remote repository and writes to file.
   *
   * @param args [/path/to/config/file] <branch> <commit>
   * @apiNote defaults to config.json in the project directory.
   */
  public static void main(String[] args) throws Exception {
    args = new String[]{"./config.json"};
    if (args.length != 1) {
      Error.reportAndExit(Error.INVALID_ARGS);
    }

    // Create both directories needed
    FileUtils.createPaths();

    // Initialize the irExtractionService
    IRExtractionService irExtractionService = new IRExtractionService(args[0]);

    // Generate the Intermediate Representation
    irExtractionService.generateIR();


  }
}
