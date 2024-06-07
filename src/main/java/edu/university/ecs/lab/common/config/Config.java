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

/**
 * Model to represent the JSON configuration file
 * Some additional notes, this object is p
 *
 */
@Getter
@Setter
public class Config {
  private static final String GIT_SCHEME_DOMAIN = "https://github.com/";
  private static final String GIT_PATH_EXTENSION = ".git";

  /** The name of the system analyzed */
  private final String systemName;

  /** The path to write cloned repository files to */
  private final String repositoryURL;

  /**
   * Initial starting commit for repository
   */
  private String baseCommit;

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

}
