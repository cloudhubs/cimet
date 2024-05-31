package edu.university.ecs.lab.intermediate.create;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.utils.FullCimetUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

import static edu.university.ecs.lab.common.models.enums.ErrorCodes.BAD_ARGS;

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
    if (args.length != 3 && args.length != 2) {
      System.err.println(
          "Usage: java -jar IRExtraction.jar [/path/to/config/file] <branch> <commit>");
      System.exit(BAD_ARGS.ordinal());
    }

    // Get input config
    Config config = ConfigUtil.validateConfig(args[0]);
    String baseBranch = args[1];
    String commit = null;

    if (args.length == 3) {
      commit = args[2];
    }

    IRExtractionService irExtractionService =
        new IRExtractionService(config, baseBranch, commit);
    String outputFileName = irExtractionService.generateSystemIntermediate();

    // Save the file name for the full system runner
    FullCimetUtils.pathToIR = outputFileName;
    FullCimetUtils.baseBranch = baseBranch;
    FullCimetUtils.baseCommit = commit;
  }
}
