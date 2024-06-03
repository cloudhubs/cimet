package edu.university.ecs.lab.common.config;

import edu.university.ecs.lab.common.error.Error;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static edu.university.ecs.lab.common.error.Error.*;

/** Model to represent the JSON configuration file */
@Getter
@Setter
public class Config {
  static final String RELATIVE_PROJECT_PATH = System.getProperty("user.dir");
  static final String DEFAULT_OUTPUT_PATH = "output";
  static final String DEFAULT_CLONE_PATH = "clone";
  private static final String GIT_SCHEME_DOMAIN = "https://github.com/";
  private static final String GIT_PATH_EXTENSION = ".git";

  /** The name of the system analyzed */
  private final String systemName;

  /** The path to write cloned repository files to */
  private final String repositoryURL;

  /**
   * Initial starting commit for repository
   */
  private final String baseCommit;

  /**
   * Initial starting commit for repository
   */
  private final String baseBranch;

  /**
   * Paths relative to the repository that indicate locations of microservices
   */
  private List<String> relativeMicroservicePaths;

  public Config(String systemName, String repositoryURL, List<String> relativeMicroservicePaths, String baseCommit, String baseBranch) throws Exception {
    Objects.requireNonNull(systemName, NULL_ERROR.getMessage());
    Objects.requireNonNull(repositoryURL, NULL_ERROR.getMessage());
    Objects.requireNonNull(relativeMicroservicePaths, NULL_ERROR.getMessage());
    Objects.requireNonNull(baseCommit, NULL_ERROR.getMessage());
    Objects.requireNonNull(baseBranch, NULL_ERROR.getMessage());
    validateRepositoryLink(repositoryURL);
    validateRelativeRepositoryPaths(relativeMicroservicePaths);

    this.systemName = systemName;
    this.repositoryURL = repositoryURL;
    this.relativeMicroservicePaths = relativeMicroservicePaths;
    this.baseCommit = baseCommit;
    this.baseBranch = baseBranch;
  }

  /** The list of repository objects as indicated by config */

  private void validateRepositoryLink(String repositoryLink) {
    if(!(repositoryLink.isBlank() || repositoryLink.startsWith(GIT_SCHEME_DOMAIN) || repositoryLink.endsWith(GIT_PATH_EXTENSION))) {
      Error.reportAndExit(Error.INVALID_REPOSITORY_LINK);
    }
  }

  /** The list of repository objects as indicated by config */

  private void validateRelativeRepositoryPaths(List<String> relativeMicroservicePaths) {
    if(relativeMicroservicePaths.isEmpty()) {
      Error.reportAndExit(Error.INVALID_REPO_PATHS);
    }
  }

  /**
   * This method returns the relative local path of a cloned repository as ./clonePath/repoName.
   * This will be a working relative path to the repository on the local file system.
   *
   * @return the relative path string where that repository is cloned to
   */
  public String getLocalClonePath() {
    return DEFAULT_CLONE_PATH + "/" + getRepoName();
  }

  /**
   * This method gets the repository name parsed from the repositoryURL
   *
   *
   * @return the plain string repository name with no path related characters
   */
  public String getRepoName() {
    int lastSlashIndex = repositoryURL.lastIndexOf("/");
    int lastDotIndex = repositoryURL.lastIndexOf('.');
    return repositoryURL.substring(lastSlashIndex + 1, lastDotIndex);
  }

  /**
   * This method gets local paths to each microservice in the repository based on the config file
   * structure.
   *
   * <p>Should only be called AFTER cloning the repository, as it validates the ms directories.
   * These will be working relative paths to each microservice in the repository.
   *
   * @return list of paths to the microservices in the repository .<br>
   *     <strong>Paths will be like: "./clonePath/repoName/.../serviceName"</strong>
   */
  public List<String> getMicroservicePaths() {
    List<String> returnPaths = new ArrayList<>();

    // Path "clonePath/repoName"
    String relativeClonePath = getRepoName();

    for (String relativeMicroservicePath : relativeMicroservicePaths) {

      String updatedRelativeMicroservicePath = "." + File.separator + DEFAULT_CLONE_PATH + File.separator + getRepoName() + File.separator + relativeMicroservicePath + File.separator;

      // In line validation that the relative path is a directory
      File file = new File(updatedRelativeMicroservicePath);
      if (file.isDirectory()) {
        returnPaths.add(updatedRelativeMicroservicePath);
      } else {
        System.err.println(INVALID_REPO_PATH + " " + updatedRelativeMicroservicePath);
      }
    }

    return returnPaths;
  }

//  /**
//   * This method will take any file path and return a shortened version as just
//   * <strong>"repoName/.../serviceName/.../file.java"</strong> removing the clonePath from the
//   * beginning of the path. Will not start with a "./" or a "/".
//   *
//   * @return the shortened path
//   */
//  public String getShortPath(String fullPath) {
//    return fullPath.substring(fullPath.indexOf(clonePath) + clonePath.length() + 1);
//  }


}
