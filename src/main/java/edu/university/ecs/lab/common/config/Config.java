package edu.university.ecs.lab.common.config;

import edu.university.ecs.lab.common.error.Error;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Optional;

import static edu.university.ecs.lab.common.error.Error.NULL_ERROR;

/**
 * Model to represent the JSON configuration file
 * Some additional notes, this object is p
 */
@Getter
@Setter
public class Config {
    private static final String GIT_SCHEME_DOMAIN = "https://github.com/";
    private static final String GIT_PATH_EXTENSION = ".git";

    /**
     * The name of the system analyzed
     */
    private final String systemName;

    /**
     * The path to write cloned repository files to
     */
    private final String repositoryURL;

    /**
     * Initial starting commit for repository
     */
    private final String branch;


    public Config(String systemName, String repositoryURL, String branch) throws Exception {
        validateConfig(systemName, repositoryURL, branch);

        this.systemName = systemName;
        this.repositoryURL = repositoryURL;
        this.branch = branch;
    }

    /**
     * This method
     */

    private void validateConfig(String systemName, String repositoryURL, String branch) {
        try {
            Objects.requireNonNull(systemName);
            Objects.requireNonNull(repositoryURL);
            Objects.requireNonNull(branch);
            validateConfig(systemName, repositoryURL, branch);

            assert !systemName.isBlank() && !repositoryURL.isBlank() && !branch.isBlank();
        } catch (Exception e) {
            Error.reportAndExit(Error.INVALID_CONFIG, Optional.of(e));
        }
        Objects.requireNonNull(systemName, NULL_ERROR.getMessage());
        Objects.requireNonNull(repositoryURL, NULL_ERROR.getMessage());
        Objects.requireNonNull(branch, NULL_ERROR.getMessage());
        validateRepositoryURL(repositoryURL);
    }

    /**
     * The list of repository objects as indicated by config
     */

    private void validateRepositoryURL(String repositoryURL) {
        if (!(repositoryURL.isBlank() || repositoryURL.startsWith(GIT_SCHEME_DOMAIN) || repositoryURL.endsWith(GIT_PATH_EXTENSION))) {
            Error.reportAndExit(Error.INVALID_REPOSITORY_URL, Optional.empty());
        }
    }

    /**
     * This method gets the repository name parsed from the repositoryURL
     *
     * @return the plain string repository name with no path related characters
     */
    public String getRepoName() {
        int lastSlashIndex = repositoryURL.lastIndexOf("/");
        int lastDotIndex = repositoryURL.lastIndexOf('.');
        return repositoryURL.substring(lastSlashIndex + 1, lastDotIndex);
    }

}
