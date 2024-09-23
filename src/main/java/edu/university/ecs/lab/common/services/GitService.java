package edu.university.ecs.lab.common.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.utils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for managing local repository including the cloning
 * and resetting the current commit.
 */
public class GitService {
    private static final int EXIT_SUCCESS = 0;
    private static final String HEAD_COMMIT = "HEAD";

    /**
     * Configuration file path
     */
    private final Config config;

    /**
     * Repository object for jgit usage
     */
    private final Repository repository;


    /**
     * Instantiation of this service will result in the following
     * 1.) output and clone directories will be created or validated
     * 2.) Configuration file will be read and validated by it's constructor
     * 3.) The repository in config will be cloned or validated
     *
     * @param configPath
     */
    public GitService(String configPath) {
        // 1.) Read config
        this.config = ConfigUtil.readConfig(configPath);

        // 2.) Make the output and clone directory
        FileUtils.makeDirs();

        // 3.) Clone the repository
        cloneRemote();

        // If clone was successful we can now set repo and reset local repo to config base commit
        this.repository = initRepository();

    }


    /**
     * This method clones a remote repository to the local file system. Postcondition: the repository
     * has been cloned to the local file system.
     *
     */
    public void cloneRemote() {
        // Quietly return assuming cloning already took place
        String repositoryPath = FileUtils.getRepositoryPath(config.getRepoName());

        // Quietly return assuming cloning already took place
        if (new File(repositoryPath).exists()) {
            return;
        }

        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder("git", "clone", config.getRepositoryURL(), repositoryPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();


            if (exitCode != EXIT_SUCCESS) {
                throw new Exception();
            }

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        LoggerManager.info(() -> "Cloned repository " + config.getRepoName());
    }

    /**
     * This method resets the local repository to commitID.
     * Used to initially set commit for clone and additionally to
     * advance the local repository as we step through commits
     *
     * @param commitID if empty or null, defaults to HEAD
     */
    public void resetLocal(String commitID) {
        validateLocalExists();

        // If an invalid commit is passed simply make no change
        if (Objects.isNull(commitID) || commitID.isEmpty()) {
            return;
        }

        try (Git git = new Git(repository)) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitID).call();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        LoggerManager.info(() -> "Set repository " + config.getRepoName() + " to " + commitID);


    }

    /**
     * This method validates that the local repository exists or
     * reports and exits if it doesn't.
     */
    private void validateLocalExists() {
        File file = new File(FileUtils.getRepositoryPath(config.getRepoName()));
        if (!(file.exists() && file.isDirectory())) {
            Error.reportAndExit(Error.REPO_DONT_EXIST, Optional.empty());
        }
    }

    /**
     * Establish a local endpoint for the given repository path.
     *
     * @return the repository object
     */
    public Repository initRepository() {
        validateLocalExists();

        Repository repository = null;

        try {
            File repositoryPath = new File(FileUtils.getRepositoryPath(config.getRepoName()));
            repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath, ".git")).build();

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED,Optional.of(e));
        }

        return repository;
    }


    /**
     * Get the differences between commitOld and commitNew
     *
     * @param commitOld the old commit ID
     * @param commitNew the new commit ID
     * @return the list of differences as DiffEntrys
     */
    public List<DiffEntry> getDifferences(String commitOld, String commitNew) {
        List<DiffEntry> returnList = null;
        RevCommit oldCommit = null, newCommit = null;
        RevWalk revWalk = new RevWalk(repository);

        try {
            oldCommit = revWalk.parseCommit(repository.resolve(commitOld));
            newCommit = revWalk.parseCommit(repository.resolve(commitNew));
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        // Prepare tree parsers for both commits
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            oldTreeParser.reset(reader, oldCommit.getTree());
            newTreeParser.reset(reader, newCommit.getTree());

            // Compute differences
            try (Git git = new Git(repository)) {
                returnList = git.diff()
                        .setNewTree(newTreeParser)
                        .setOldTree(oldTreeParser)
                        .call();

            }
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        LoggerManager.debug(() -> "Got differences of repository " + config.getRepoName() + " between " + commitOld + " -> " + commitNew);

        return returnList;
    }

    public Iterable<RevCommit> getLog() {
        Iterable<RevCommit> returnList = null;

        try (Git git = new Git(repository)) {
            returnList = git.log().call();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        return returnList;
    }

    public String getHeadCommit() {
        String commitID = "";

        try {
            // Get the reference to HEAD
            Ref head = repository.findRef(HEAD_COMMIT);
            RevWalk walk = new RevWalk(repository);

            // Use RevWalk to parse the commit
            ObjectId commitId = head.getObjectId();
            RevCommit commit = walk.parseCommit(commitId);

            commitID = commit.getName();

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        return commitID;
    }


}
