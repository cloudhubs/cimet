package edu.university.ecs.lab.delta;

import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.services.LoggerManager;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.Optional;

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
        String[] finalArgs = args;
        LoggerManager.info(() -> "DeltaExtractionRunner starting... args: " + Arrays.toString(finalArgs));
        if (args.length != 3) {
            Error.reportAndExit(Error.INVALID_ARGS, Optional.empty());
        }

        DeltaExtractionService deltaService = new DeltaExtractionService(args[0], "./output/OldIR.json", args[1], args[2]);

        deltaService.generateDelta();

    }
}
