package edu.university.ecs.lab.delta.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.util.List;

/**
 * Service for extracting the differences between two commits of a repository
 */
public class DeltaExtractionService {

    /**
     * Config object representing the contents of the config file
     */
    private final Config config;

    /**
     * GitService instance for interacting with the local repository
     */
    private final GitService gitService;

    /**
     * The old commit for comparison
     */
    private final String commitOld;

    /**
     * The new commit for comparison
     */
    private final String commitNew;


    /**
     * Constructor for the DeltaExtractionService
     *
     * @param configPath path to the config file
     * @param commitOld old commit for comparison
     * @param commitNew new commit for comparison
     */
    public DeltaExtractionService(String configPath, String commitOld, String commitNew) {
        this.config = ConfigUtil.readConfig(configPath);
        this.gitService = new GitService(configPath);
        this.commitOld = commitOld;
        this.commitNew = commitNew;
    }

    /**
     * Generates Delta file representing changes between commitOld and commitNew
     */
    public void generateDelta() {
        List<DiffEntry> differences = null;

        // Ensure we start at commitOld
        gitService.resetLocal(commitOld);

        try {
            differences = gitService.getDifferences(commitOld, commitNew);

        } catch (Exception e) {
            Error.reportAndExit(Error.GIT_FAILED);
        }

        // Advance the local commit for parsing
        gitService.resetLocal(commitNew);

        // process/write differences to delta output
        processDelta(differences);

    }

    /**
     * Process the differences between the local and remote repository and write the differences to a
     * file.
     *
     * @param diffEntries the list of differences extracted by GitService
     */
    public void processDelta(List<DiffEntry> diffEntries) {

        // Set up a new SystemChangeObject
        SystemChange systemChange = new SystemChange();
        systemChange.setOldCommit(commitOld);
        systemChange.setNewCommit(commitNew);

        // process each difference
        for (DiffEntry entry : diffEntries) {

            if (commitNew.startsWith("a78")) {
                System.out.println("RENAME" + entry);
            }

            // If its not a java file and doesnt end with pom.xml
            String path = entry.getChangeType().equals(DiffEntry.ChangeType.DELETE) ? entry.getOldPath() : entry.getNewPath();

            // If paths doesnt end with java or (path doesnt end with java or pom)
            if (!path.endsWith(".java") && !path.endsWith("pom.xml")) {
                continue;
            }

            String oldPath = "";
            String newPath = "";

            if (DiffEntry.ChangeType.DELETE.equals(entry.getChangeType())) {
                oldPath = FileUtils.GIT_SEPARATOR + entry.getOldPath();
                newPath = null;

            } else if (DiffEntry.ChangeType.ADD.equals(entry.getChangeType())) {
                oldPath = null;
                newPath = FileUtils.GIT_SEPARATOR + entry.getNewPath();

            } else {
                oldPath = FileUtils.GIT_SEPARATOR + entry.getOldPath();
                newPath = FileUtils.GIT_SEPARATOR + entry.getNewPath();

            }


            // Get the class, if we are a delete the file for parsing no longer exists
            // If we are a pom.xml we cannot parse
            JClass jClass = null;
            if (!path.endsWith("pom.xml")) {

                if (!entry.getChangeType().equals(DiffEntry.ChangeType.DELETE)) {

                    jClass = SourceToObjectUtils.parseClass(new File(FileUtils.gitPathToLocalPath(newPath, config.getRepoName())), config);

                    // If we try to parse and it is still null, for ADD we will skip
                    if (jClass == null && entry.getChangeType().equals(DiffEntry.ChangeType.ADD)) {
                        continue;
                    }

                    // For MODIFY we will let pass since it might be modifying a previously accepted file

                }

            }


            // If the class isn't ours and it isn't a folder or Docker or Pom

            systemChange.getChanges().add(new Delta(oldPath, newPath, ChangeType.fromDiffEntry(entry), jClass));

        }

        JsonReadWriteUtils.writeToJSON("./output/Delta.json", systemChange);

        System.out.println("Delta extracted: from " + commitOld + " to " + commitNew + " at ./output/Delta.json");

    }


}
