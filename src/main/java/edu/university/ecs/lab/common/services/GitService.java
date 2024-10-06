package edu.university.ecs.lab.common.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.utils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to perform Git opperations
 */
public class GitService {
    private static final int EXIT_SUCCESS = 0;
    private static final String HEAD_COMMIT = "HEAD";

    private final Config config;
    private final Repository repository;

    /**
     * Create a Git service object from a project configuration file
     * 
     * @param configPath path to project configuration file
     */
    public GitService(String configPath) {
        this.config = ConfigUtil.readConfig(configPath);
        FileUtils.makeDirs();
        cloneRemote();
        this.repository = initRepository();
    }

    /**
     * Method to clone a repository
     */
    public void cloneRemote() {
        String repositoryPath = FileUtils.getRepositoryPath(config.getRepoName());

        // Check if repository was already cloned
        if (new File(repositoryPath).exists()) {
            return;
        }

        // Create and execute operating system process to clone repository
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
     * Method to reset repository to a given commit
     * 
     * @param commitID commit id to reset to
     */
    public void resetLocal(String commitID) {
        validateLocalExists();

        if (Objects.isNull(commitID) || commitID.isEmpty()) {
            return;
        }

        // Reset branch to old commit
        try (Git git = new Git(repository)) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitID).call();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        LoggerManager.info(() -> "Set repository " + config.getRepoName() + " to " + commitID);
    }

    /**
     * Method to check that local directory exists
     */
    private void validateLocalExists() {
        File file = new File(FileUtils.getRepositoryPath(config.getRepoName()));
        if (!(file.exists() && file.isDirectory())) {
            Error.reportAndExit(Error.REPO_DONT_EXIST, Optional.empty());
        }
    }

    /**
     * Method to initialize repository from repository name
     * 
     * @return file repository
     */
    public Repository initRepository() {
        validateLocalExists();

        Repository repository = null;

        try {
            File repositoryPath = new File(FileUtils.getRepositoryPath(config.getRepoName()));
            repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath, ".git")).build();

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        return repository;
    }

    /**
     * Method to get differences between old and new commits
     * 
     * @param commitOld old commit id
     * @param commitNew new commit id
     * @return list of changes from old commit to new commit
     */
    public List<DiffEntry> getDifferences(String commitOld, String commitNew) {
        List<DiffEntry> returnList = null;
        RevCommit oldCommit = null, newCommit = null;
        RevWalk revWalk = new RevWalk(repository);

        try {
            // Parse the old and new commits
            oldCommit = revWalk.parseCommit(repository.resolve(commitOld));
            newCommit = revWalk.parseCommit(repository.resolve(commitNew));
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        // Prepare tree parsers for both commits
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();

            // Use tree objects from the commits
            oldTreeParser.reset(reader, oldCommit.getTree().getId());
            newTreeParser.reset(reader, newCommit.getTree().getId());

            // Compute differences between the trees of the two commits
            try (Git git = new Git(repository)) {
                List<DiffEntry> rawDiffs = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .call();

                // Filter out diffs that only contain whitespace or comment changes
                RevCommit finalOldCommit = oldCommit;
                RevCommit finalNewCommit = newCommit;
                returnList = rawDiffs.stream()
                        .filter(diff -> isCodeChange(diff, repository, finalOldCommit, finalNewCommit))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        LoggerManager.debug(() -> "Got differences of repository " + config.getRepoName() + " between " + commitOld + " -> " + commitNew);

        return returnList;
    }

    /**
     * Method to check if a commit difference was a change to the code
     * 
     * @param diff DiffEntry object
     * @param repository repository to check
     * @param oldCommit old commit id
     * @param newCommit new commit id
     * 
     * @return true if difference was a change to the code, false otherwise
     */
    private boolean isCodeChange(DiffEntry diff, Repository repository, RevCommit oldCommit, RevCommit newCommit) {
        if((!diff.getOldPath().endsWith(".java") && !diff.getNewPath().endsWith(".java"))) {
            return true;
        }

        // Read the file contents before and after the changes
        String oldContent = getContentFromTree(repository, oldCommit.getTree().getId(), diff.getOldPath());
        String newContent = getContentFromTree(repository, newCommit.getTree().getId(), diff.getNewPath());

        // Remove comments and whitespace from both contents
        String oldCode = stripCommentsAndWhitespace(oldContent);
        String newCode = stripCommentsAndWhitespace(newContent);

        // If the meaningful code is different, return true
        return !oldCode.equals(newCode);
    }

    /**
     * Get file data from a file tree
     * 
     * @param repository repository to check
     * @param treeId id of the tree to check
     * @param filePath file to get data from
     * @return data from the file, or an empty string if an error occurs or file is not found
     */
    private String getContentFromTree(Repository repository, ObjectId treeId, String filePath) {
        try (ObjectReader reader = repository.newObjectReader();
             TreeWalk treeWalk = new TreeWalk(repository)) {

            // Add the tree to the tree walker
            treeWalk.addTree(treeId);
            treeWalk.setRecursive(true); // We want to search recursively

            // Walk through the tree to find the file
            while (treeWalk.next()) {
                String currentPath = treeWalk.getPathString();
                if (currentPath.equals(filePath)) {
                    // Ensure we have a blob (file) and not a tree
                    if (treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB) {
                        // Read the file content and return it
                        byte[] data = reader.open(treeWalk.getObjectId(0)).getBytes();
                        return new String(data, StandardCharsets.UTF_8);
                    }
                }
            }

        } catch (Exception e) {
            // Return an empty string in case of an error
            return "";
        }

        // If the file is not found, return an empty string
        return "";
    }

    /**
     * Remove comments and whitespace from file content
     * 
     * @param content string of all file content
     * @return string of file content with whitespace and comments removed
     */
    private String stripCommentsAndWhitespace(String content) {
        return content.replaceAll("(//.*|/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/|\\s+)", "");
    }

    /**
     * Get Git log
     * 
     * @return Git log as a list
     */
    public Iterable<RevCommit> getLog() {
        Iterable<RevCommit> returnList = null;

        try (Git git = new Git(repository)) {
            returnList = git.log().call();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        return returnList;
    }

    /**
     * Get head commit for the repository
     * 
     * @return commit id of head commit
     */
    public String getHeadCommit() {
        String commitID = "";

        try {
            Ref head = repository.findRef(HEAD_COMMIT);
            RevWalk walk = new RevWalk(repository);
            ObjectId commitId = head.getObjectId();
            RevCommit commit = walk.parseCommit(commitId);
            commitID = commit.getName();
            walk.close();
        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED, Optional.of(e));
        }

        return commitID;
    }
}
