package edu.university.ecs.lab.common.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.utils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Service for managing local repositories including the cloning
 * and resetting the current commit.
 */
public class GitService {
    private static final int EXIT_SUCCESS = 0;
    private static final String HEAD_COMMIT = "HEAD";


    /**
     * Configuration file path
     */
    private final Config config;

    private final Repository repository;

    public GitService(String configPath) {
        this.config = ConfigUtil.readConfig(configPath);
        cloneRemote();

        // If clone was successful we can now set repo and reset local repo to config base commit
        this.repository = initRepository();
        resetLocal(config.getBaseCommit());

    }

    // TODO REMOVE THIS IS FOR TESTING ONLY
    public GitService(Config config) {
        this.config = config;
        cloneRemote();

        // If clone was successful we can now set repo and reset local repo to config base commit
        this.repository = initRepository();
        resetLocal(config.getBaseCommit());

    }

    /**
     * This method clones a remote repository to the local file system. Postcondition: the repository
     * has been cloned to the local file system.
     *
     * @throws Exception if Git clone failed
     */
    public void cloneRemote() {

        // Quietly return assuming cloning already took place
        File file = new File(FileUtils.getClonePath(config.getRepoName()));
        if (file.exists()) {
            return;
        }

        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder("git", "clone", config.getRepositoryURL(), FileUtils.getClonePath(config.getRepoName()));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();


            if (exitCode != EXIT_SUCCESS) {
                throw new Exception();
            }

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED);
        }
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

        if (Objects.isNull(commitID) || commitID.isEmpty()) {
            commitID = HEAD_COMMIT;
        }

        try (Git git = new Git(repository)) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitID).call();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED);
        }

    }

    /**
     * This method reset's the local branch to a relative commit
     * from head
     *
     * @param relativeIndex if empty or null, defaults to HEAD
     * @return boolean indicating success
     */
    public boolean resetLocal(int relativeIndex) {

        try (Git git = new Git(repository)) {
            String relativeHead = HEAD_COMMIT + "~" + relativeIndex;
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(relativeHead).call();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * This method validates that the local repository exists or
     * reports and exits if it doesn't.
     */
    private void validateLocalExists() {
        File file = new File(FileUtils.getBaseClonePath());
        if (!(file.exists() && file.isDirectory())) {
            Error.reportAndExit(Error.REPO_DONT_EXIST);
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
            File localRepoDir = new File(FileUtils.getClonePath(config.getRepoName()));
            repository = new FileRepositoryBuilder().setGitDir(new File(localRepoDir, ".git")).build();

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED);
        }

        return repository;
    }

    /**
     * Get the differences between local branch defined by config and branch + 1.
     *
     * @param relativeIndex the relative index from head that we will compare head to
     * @return the list of differences
     */
    public List<DiffEntry> getDifferences(String commitOld, String commitNew) throws Exception {
        List<DiffEntry> returnList = null;

        RevWalk revWalk = new RevWalk(repository);
//    String relativeHead = HEAD_COMMIT + "~" + relativeIndex;
        RevCommit oldCommit = revWalk.parseCommit(repository.resolve(commitOld));
        RevCommit newCommit = revWalk.parseCommit(repository.resolve(commitNew));

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
            Error.reportAndExit(Error.GIT_FAILED);
        }

        return returnList;
    }

    public Iterable<RevCommit> getLog() {
        Iterable<RevCommit> returnList = null;

        try (Git git = new Git(repository)) {
            returnList = git.log().call();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED);
        }


        return returnList;
    }


}
