package edu.university.ecs.lab.intermediate.create;

import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.services.LoggerManager;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

import java.util.Arrays;
import java.util.Optional;


/**
 *
 * <p>The IR extraction process is responsible for cloning remote services, scanning through each
 * local repo and extracting rest endpoints/calls, and writing each service and endpoints to
 * intermediate representation.</p>
 */
public class IRExtractionRunner {

    /**
     * Intermediate extraction runner, generates IR from remote repository and writes to file.
     *
     */
    public static void main(String[] args) throws Exception {
        args = new String[]{"./config.json"};
        String[] finalArgs = args;
        LoggerManager.info(() -> "IRExtractionRunner starting... args: " + Arrays.toString(finalArgs));
        if (args.length != 1) {
            Error.reportAndExit(Error.INVALID_ARGS, Optional.empty());
        }


        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(args[0], Optional.empty());

        // Generate the Intermediate Representation
        irExtractionService.generateIR("IR.json");

    }
}
