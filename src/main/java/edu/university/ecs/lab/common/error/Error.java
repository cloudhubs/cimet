package edu.university.ecs.lab.common.error;

public enum Error {
    UNKNOWN_ERROR(1, "Unknown error has occured!"),
    NULL_ERROR(1, "Input cannot be null!"),
    INVALID_REPOSITORY_LINK(2, "Invalid repository link!"),
    INVALID_REPO_PATHS(3, "Invalid relative repository paths!"),
    INVALID_REPO_PATH(4, "Invalid repository relative path after update! Skipping!"),
    INVALID_CONFIG_PATH(5, "Invalid configuration file path!"),
    REPO_DONT_EXIST(6, "The specified repository does not exist!"),
    GIT_FAILED(7, "The requested git action failed for an unknown reason!");

    private final int code;
    private final String message;

    Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static void reportAndExit(Error error) {
        System.err.println(error.getMessage());
        System.exit(error.code);
    }

    @Override
    public String toString() {
        return "Error " + code + ": " + message;
    }
}
