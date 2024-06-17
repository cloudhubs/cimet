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
 * Service for extracting the differences between a local and remote repository and generating delta
 */
public class DeltaExtractionService {

    /**
     * Config file, defaults to config.json
     */
    private final Config config;

    /**
     * Config file, defaults to config.json
     */
    private final GitService gitService;

    private final String commitOld;

    private final String commitNew;


    /**
     * Constructor for the delta extraction service
     *
     * @param configPath file path to the configuration file
     */
    public DeltaExtractionService(String configPath, String commitOld, String commitNew) {
        this.gitService = new GitService(configPath);
        this.config = ConfigUtil.readConfig(configPath);
        this.commitOld = commitOld;
        this.commitNew = commitNew;
    }

    /**
     * Generate delta between base branch and base branch + 1
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
        generateDelta(differences);

    }

    /**
     * Process the differences between the local and remote repository and write the differences to a
     * file.
     *
     * @param diffEntries the list of differences extracted
     */
    public void generateDelta(List<DiffEntry> diffEntries) {

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
                oldPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getOldPath().replace("/", File.separator);
                newPath = null;

            } else if (DiffEntry.ChangeType.ADD.equals(entry.getChangeType())) {
                oldPath = null;
                newPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getNewPath().replace("/", File.separator);

            } else {
                oldPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getOldPath().replace("/", File.separator);
                newPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getNewPath().replace("/", File.separator);

            }

            String microserviceName = null;


            // Get the class, if we are a delete the file for parsing no longer exists
            // If we are a pom.xml we cannot parse
            JClass jClass = null;
            if (!path.endsWith("pom.xml")) {

                if (!entry.getChangeType().equals(DiffEntry.ChangeType.DELETE)) {

                    jClass = getClass(newPath);

                    // If we try to parse and it is still null, for ADD we will skip
                    if (jClass == null && entry.getChangeType().equals(DiffEntry.ChangeType.ADD)) {
                        continue;
                    }

                    // For MODIFY we will let pass since it might be modifying a previously accepted file

                }

            }


            // If the class isn't ours and it isn't a folder or Docker or Pom

            systemChange.getChanges().add(createDelta(oldPath, newPath, entry, jClass, "", ""));

        }

        JsonReadWriteUtils.writeToJSON("./output/Delta.json", systemChange);

        System.out.println("Delta extracted: from " + commitOld + " to " + commitNew + " at ./output/Delta.json");

    }


    private JClass getClass(String localPath) {
        return SourceToObjectUtils.parseClass(new File(localPath), config);

    }


    private Delta createDelta(String oldPath, String newPath, DiffEntry entry, JClass jClass, String oldMicroserviceName, String newMicroserviceName) {
        return new Delta(oldPath, newPath, ChangeType.fromDiffEntry(entry), jClass, oldMicroserviceName, newMicroserviceName);
    }

}
