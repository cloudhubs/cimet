package edu.university.ecs.lab.delta.services;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.services.LoggerManager;
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
 * Service for extracting the differences between two commits of a repository.
 * This class does cleaning of output so not all changes will be reflected in
 * the Delta output file.
 */
public class DeltaExtractionService {
    private static final String DEV_NULL = "/dev/null";
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
     * System change object that will be returned
     */
    private SystemChange systemChange;

    /**
     * The type of change that is made
     */
    private ChangeType changeType;

    /**
     * The path to the output file
     */
    private String outputPath;


    /**
     * Constructor for the DeltaExtractionService
     *
     * @param configPath path to the config file
     * @param outputPath output path for file
     * @param commitOld old commit for comparison
     * @param commitNew new commit for comparison
     */
    public DeltaExtractionService(String configPath, String outputPath, String commitOld, String commitNew) {
        this.config = ConfigUtil.readConfig(configPath);
        this.gitService = new GitService(configPath);
        this.commitOld = commitOld;
        this.commitNew = commitNew;
        this.outputPath = outputPath.isEmpty() ? "./Delta.json" : outputPath;
    }

    /**
     * Generates Delta file representing changes between commitOld and commitNew
     */
    public void generateDelta() {
        List<DiffEntry> differences = null;

        // Ensure we start at commitOld
        gitService.resetLocal(commitOld);

        // Get the differences between commits
        differences = gitService.getDifferences(commitOld, commitNew);

        // Advance the local commit for parsing
        gitService.resetLocal(commitNew);

        // process/write differences to delta output
        processDelta(differences);

    }

    /**
     * Process differences between commits
     * 
     * @param diffEntries list of differences
     */
    public void processDelta(List<DiffEntry> diffEntries) {
        // Set up a new SystemChangeObject
        systemChange = new SystemChange();
        systemChange.setOldCommit(commitOld);
        systemChange.setNewCommit(commitNew);
        JsonObject data = null;


        // process each difference
        for (DiffEntry entry : diffEntries) {
            // Git path
            String path = entry.getChangeType().equals(DiffEntry.ChangeType.ADD) ? entry.getNewPath() : entry.getOldPath();

            // Special case for root pom
            if(path.equals("pom.xml")) {
                continue;
            }

            // Guard condition, skip invalid files
            if(!FileUtils.isValidFile(path)) {
               continue;
            }

            // Setup oldPath, newPath for Delta
            String oldPath = "";
            String newPath = "";

            if (DiffEntry.ChangeType.DELETE.equals(entry.getChangeType())) {
                oldPath = FileUtils.GIT_SEPARATOR + entry.getOldPath();
                newPath = DEV_NULL;

            } else if (DiffEntry.ChangeType.ADD.equals(entry.getChangeType())) {
                oldPath = DEV_NULL;
                newPath = FileUtils.GIT_SEPARATOR + entry.getNewPath();

            } else {
                oldPath = FileUtils.GIT_SEPARATOR + entry.getOldPath();
                newPath = FileUtils.GIT_SEPARATOR + entry.getNewPath();

            }

            changeType = ChangeType.fromDiffEntry(entry);

            switch(changeType) {
                case ADD:
                    data = add(newPath);
                    break;
                case MODIFY:
                    data = add(oldPath);
                    break;
                case DELETE:
                    data = delete();
            }

            systemChange.getChanges().add(new Delta(oldPath, newPath, changeType, data));
        }

        // Output the system changes
        JsonReadWriteUtils.writeToJSON(outputPath, systemChange);

        // Report
        LoggerManager.info(() -> "Delta changes extracted between " + commitOld + " -> " + commitNew);

    }

    /**
     * This method parses a newly added file into a JsonObject containing
     * the data of the change (updated file). Returns a blank JsonObject if
     * parsing fails (returns null).
     *
     * @param newPath git path of new file
     * @return JsonObject of data of the new file
     */
    private JsonObject add(String newPath) {
        // Check if it is a configuration file
        if(FileUtils.isConfigurationFile(newPath)) {
            ConfigFile configFile = SourceToObjectUtils.parseConfigurationFile(new File(FileUtils.gitPathToLocalPath(newPath, config.getRepoName())), config);
            if(configFile == null || configFile.getData() == null) {
                return new JsonObject();
            } else {
                return configFile.toJsonObject();
            }

        // Else it is a Java file
        } else {
            JClass jClass = SourceToObjectUtils.parseClass(new File(FileUtils.gitPathToLocalPath(newPath, config.getRepoName())), config, "");
            if(jClass == null) {
                return new JsonObject();
            } else {
                return jClass.toJsonObject();
            }
        }

    }

    /**
     * This method returns a blank JsonObject() as there is no data to parse
     *
     * @return JsonObject that is empty
     */
    private JsonObject delete() {
        return new JsonObject();
    }




}
