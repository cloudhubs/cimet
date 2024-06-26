package edu.university.ecs.lab.delta;

import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;

/**
 * This class acts as a runner implementation for extracting a Delta file
 */
public class DeltaExtractionRunner {
    /**
     * This method compares two commits on the specified branch in the config
     *
     * @param args {@literal [/path/to/config] <oldCommit> <newCommit> }
     */
    public static void main(String[] args) throws Exception {
        args = new String[]{"./config.json", "06f3e1efe2e2539d05d91b0699cc8d9fe7be29d7", "82949fa07dcf82f66641f5807d629d15bab663a6"};
        if (args.length != 3) {
            Error.reportAndExit(Error.INVALID_ARGS);
        }

        DeltaExtractionService deltaService = new DeltaExtractionService(args[0], args[1], args[2], args[4]);

        deltaService.generateDelta();

    }
}
