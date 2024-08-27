package edu.university.ecs.lab.common.error;

import edu.university.ecs.lab.common.services.LoggerManager;
import lombok.Getter;

import java.util.Optional;

/**
 * Enum representing different error types with corresponding error codes and messages.
 */
@Getter
public enum Error {
    UNKNOWN_ERROR(1, "Unknown error has occured!"),
    NULL_ERROR(1, "Input cannot be null!"),
    INVALID_REPOSITORY_URL(2, "Invalid repository URL!"),
    INVALID_REPO_PATHS(3, "Invalid relative repository paths!"),
    INVALID_REPO_PATH(4, "Invalid repository relative path after update! Skipping!"),
    INVALID_CONFIG_PATH(5, "Invalid configuration file path!"),
    REPO_DONT_EXIST(6, "The specified repository does not exist!"),
    GIT_FAILED(7, "The requested git action failed for an unknown reason!"),
    INVALID_ARGS(8, "Invalid arguments!"),
    INVALID_JSON_READ(9, "Unable to read JSON from file!"),
    INVALID_JSON_WRITE(10, "Unable to write JSON to file!"),
    JPARSE_FAILED(10, "Failed to parse Java Code!"),
    INVALID_CONFIG(10, "Invalid configuration file!");

    /**
     *  The unique error code identifying the error type.
     */
    private final int code;
    /**
     *  The detailed message describing the error.
     */
    private final String message;

    /**
     * Constructor for Error enum.
     *
     * @param code    The error code.
     * @param message The error message.
     */
    Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Prints the error message to standard error and exits the program with the error code.
     *
     * @param error The error enum value to report and exit with.
     */
    public static void reportAndExit(Error error, Optional<Exception> exception) {
        LoggerManager.error(error::getMessage, exception);
        System.exit(error.code);
    }

    /**
     * Returns a string representation of the error.
     *
     * @return The formatted string representation of the error.
     */
    @Override
    public String toString() {
        return "Error " + code + ": " + message;
    }
}
