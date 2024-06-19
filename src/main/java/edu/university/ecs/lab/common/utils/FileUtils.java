package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.error.Error;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages all file paths and file path conversion functions.
 */
public class FileUtils {
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String DEFAULT_OUTPUT_PATH = "output";
    private static final String DEFAULT_CLONE_PATH = "clone";
    private static final String DOT = ".";
    public static final String GIT_SEPARATOR = "/";

    /**
     * Private constructor to prevent instantiation.
     */
    private FileUtils() {}

    /**
     * This method returns the relative path of the cloned repository directory as ./DEFAULT_CLONE_PATH/repoName.
     * This will be a working relative path to the repository directory on the local file system.
     *
     * @param repoName the name of the repo
     * @return the relative path string where that repository is cloned to
     */
    public static String getClonePath(String repoName) {
        return getBaseClonePath() + SEPARATOR + repoName;
    }

    /**
     * This method returns the relative local path of the output directory as ./DEFAULT_OUTPUT_PATH.
     * This will be a working relative path to the output directory on the local file system.
     *
     * @return the relative path string where the output will exist
     */
    public static String getBaseOutputPath() {
        return DOT + SEPARATOR + DEFAULT_OUTPUT_PATH;
    }

    /**
     * This method returns the local path of the output directory as ./DEFAULT_OUTPUT_PATH.
     * This will be a working relative path to the output directory on the local file system.
     *
     * @return the relative path string where the output will exist
     */
    public static String getOutputPath(String repoName) {
        return getBaseOutputPath() + SEPARATOR + repoName;
    }

    /**
     * This method returns the relative local path of the output directory as ./DEFAULT_OUTPUT_PATH.
     * This will be a working relative path to the output directory on the local file system.
     *
     * @return the relative path string where the output will exist
     */
    public static String getBaseClonePath() {
        return DOT + SEPARATOR + DEFAULT_CLONE_PATH;
    }

    /**
     * This method converts a path of the form .\clone\repoName\pathToFile to the form
     * /pathToFile
     *
     * @param localPath the local path to be converted
     * @param repoName the name of the repo cloned locally
     * @return the relative repo path
     */
    public static String localPathToGitPath(String localPath, String repoName) {
        return localPath.replace(FileUtils.getClonePath(repoName), "").replaceAll(SEPARATOR.replace("\\","\\\\"), GIT_SEPARATOR);
    }
    /**
     * This method converts a path of the form .\clone\repoName\pathToFile to the form
     * /pathToFile
     *
     * @param localPath the local path to be converted
     * @param repoName the name of the repo cloned locally
     * @return the relative repo path
     */
    public static String gitPathToLocalPath(String localPath, String repoName) {
        return getClonePath(repoName) + localPath.replace(GIT_SEPARATOR, SEPARATOR);
    }



    public static String getMicroserviceNameFromPath(String path) {
        if (!path.startsWith("." + SEPARATOR + DEFAULT_CLONE_PATH + SEPARATOR)) {
            Error.reportAndExit(Error.INVALID_REPO_PATHS);
        }

        return path.replace("." + SEPARATOR + DEFAULT_CLONE_PATH + SEPARATOR, "").split("\\\\")[1];
    }

    /**
     * This method creates the default output and clone paths
     */
    public static void createPaths() {
        try {
            new File(getBaseOutputPath()).mkdirs();
            new File(getBaseClonePath()).mkdirs();
        } catch (Exception e) {
            Error.reportAndExit(Error.INVALID_REPO_PATHS);
        }
    }

}
