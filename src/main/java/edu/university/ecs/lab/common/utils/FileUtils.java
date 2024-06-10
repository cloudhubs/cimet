package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.error.Error;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static edu.university.ecs.lab.common.error.Error.UNKNOWN_ERROR;

/**
 * Manages all file paths and file path conversion functions.
 */
public class FileUtils {
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String DEFAULT_OUTPUT_PATH = "output";
    private static final String DEFAULT_CLONE_PATH = "clone";
    private static final String DOT = ".";

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
     * This method returns the relative local path of the output directory as ./DEFAULT_OUTPUT_PATH.
     * This will be a working relative path to the output directory on the local file system.
     *
     * @return the relative path string where the output will exist
     */
    public static String getBaseClonePath() {
        return DOT + SEPARATOR + DEFAULT_CLONE_PATH;
    }

    /**
     * This method will convert a local file path to a path that is relative
     * to the repository root not including the name of the repo
     *
     * @param localPath the local path to be converted
     * @return the relative repo path
     */
    public static String pathToRepoPath(String localPath, String repoName) {
        if(localPath == null || localPath.isEmpty()) {
            return "";
        }

        return localPath.replace(DOT + SEPARATOR +  DEFAULT_CLONE_PATH + SEPARATOR + repoName, "");
    }

    /**
     * This method will convert a repo path that is relative to the repository
     * root to a local file path
     *
     * @return the relative path string where the output will exist
     */
    @Deprecated
    public static String repoPathToPath(String repoPath) {
        return DOT + SEPARATOR + DEFAULT_OUTPUT_PATH;
    }

    /**
     * This method will convert an absolute path to a path that
     * is relative to the PROJECT_PATH (user.dir)
     *
     * @param absolutePath the absolute path to be converted
     * @return the relative path string after conversion
     */
    public static String absoluteToRelative(String absolutePath) {
        Path currentDirectory = Paths.get(PROJECT_PATH);
        Path absoluteFilePath = Paths.get(absolutePath);

        // Make the absolute path relative to the current directory
        Path relativePath = currentDirectory.relativize(absoluteFilePath);

        return relativePath.toString();
    }

    public static String getMicroserviceNameFromPath(String path) {
        if(!path.startsWith("." + File.separator + DEFAULT_CLONE_PATH + File.separator)) {
            Error.reportAndExit(UNKNOWN_ERROR);
        }

        return path.replace("." + File.separator + DEFAULT_CLONE_PATH + File.separator, "").split("\\\\")[0];
    }

    public static void createPaths() {
        try {
            new File(getBaseOutputPath()).mkdirs();
            new File(getBaseClonePath()).mkdirs();
        } catch (Exception e) {
            Error.reportAndExit(Error.UNKNOWN_ERROR);
        }
    }
}
