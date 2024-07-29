package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.error.Error;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Manages all file paths and file path conversion functions.
 */
public class FileUtils {
    public static final Set<String> VALID_FILES = Set.of("pom.xml", ".java", ".yml", "build.gradle");
    public static final String SYS_SEPARATOR = System.getProperty("file.separator");
    public static final String SPECIAL_SEPARATOR = SYS_SEPARATOR.replace("\\", "\\\\");
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
    public static String getRepositoryPath(String repoName) {
        return getClonePath() + SYS_SEPARATOR + repoName;
    }

    /**
     * This method returns the relative local path of the output directory as ./DEFAULT_OUTPUT_PATH.
     * This will be a working relative path to the output directory on the local file system.
     *
     * @return the relative path string where the output will exist
     */
    public static String getOutputPath() {
        return DOT + SYS_SEPARATOR + DEFAULT_OUTPUT_PATH;
    }

    /**
     * This method returns the relative local path of the output directory as ./DEFAULT_OUTPUT_PATH.
     * This will be a working relative path to the output directory on the local file system.
     *
     * @return the relative path string where the output will exist
     */
    public static String getClonePath() {
        return DOT + SYS_SEPARATOR + DEFAULT_CLONE_PATH;
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
        return localPath.replace(FileUtils.getRepositoryPath(repoName), "").replaceAll(SPECIAL_SEPARATOR, GIT_SEPARATOR);
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
        return getRepositoryPath(repoName) + localPath.replace(GIT_SEPARATOR, SYS_SEPARATOR);
    }



    @Deprecated
    public static String getMicroserviceNameFromPath(String path) {
        if (!path.startsWith(DOT + SYS_SEPARATOR + DEFAULT_CLONE_PATH + SYS_SEPARATOR)) {
            Error.reportAndExit(Error.INVALID_REPO_PATHS, Optional.empty());
        }

        String[] split = path.replace(DOT + SYS_SEPARATOR + DEFAULT_CLONE_PATH + SYS_SEPARATOR, "").split(SPECIAL_SEPARATOR);
        return split[split.length-1];
    }

    /**
     * This method returns a Git path without the filename at the end.
     *
     * @param path the path to remove filename from
     * @return the path without the file name or if too short just GIT_SEPARATOR
     */
    public static String getGitPathNoFileName(String path) {
        String[] split = path.split(GIT_SEPARATOR);

        if(split.length > 1) {
            return String.join(GIT_SEPARATOR, Arrays.copyOfRange(split, 0, split.length - 1));
        } else {
            return GIT_SEPARATOR;
        }
    }

    /**
     * This method creates the default output and clone directories
     */
    public static void makeDirs() {
        try {
            new File(getOutputPath()).mkdirs();
            new File(getClonePath()).mkdirs();
        } catch (Exception e) {
            Error.reportAndExit(Error.INVALID_REPO_PATHS, Optional.of(e));
        }
    }

    /**
     * This method filters the file's that should be present in the project
     *
     * @param path the file for checking
     * @return boolean true if it belongs in the project
     */
    public static boolean isValidFile(String path) {
        // Special check for github metadata files
        if(path.contains(".github")) {
            return false;
        }

        for(String f : VALID_FILES) {
            if(path.endsWith(f)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method filters the static files present in the project,
     * not including Java source file but configuration files only
     *
     * @param path the file for checking
     * @return boolean true if it is a configuration file
     */
    public static boolean isConfigurationFile(String path) {
        return isValidFile(path) && !path.endsWith(".java");
    }

}
