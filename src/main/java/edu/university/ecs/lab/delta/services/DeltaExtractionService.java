package edu.university.ecs.lab.delta.services;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.services.LoggerManager;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import org.eclipse.jgit.diff.DiffEntry;

import javax.json.Json;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
     * The old IR for validating delta file changes
     */
//    private final MicroserviceSystem oldSystem;

    /**
     * System change object that will be returned
     */
    private SystemChange systemChange;

    /**
     * Represents a list of diff entries that are related to pom.xml add or delete
     */
//    private final List<DiffEntry> pomDiffs;

    /**
     * The type of change that is made
     */
    private ChangeType changeType;


    /**
     * Constructor for the DeltaExtractionService
     *
     * @param configPath path to the config file
     * @param oldIRPath path to the oldIR
     * @param commitOld old commit for comparison
     * @param commitNew new commit for comparison
     */
    public DeltaExtractionService(String configPath, String oldIRPath, String commitOld, String commitNew) {
        this.config = ConfigUtil.readConfig(configPath);
        this.gitService = new GitService(configPath);
        this.commitOld = commitOld;
        this.commitNew = commitNew;
//        this.oldSystem = JsonReadWriteUtils.readFromJSON(oldIRPath, MicroserviceSystem.class);
//        pomDiffs = new ArrayList<>();
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

            // Special check for pom file manipulations only
//            if(path.endsWith("/pom.xml")) {
//                // Get some configuration change
//                data = configurationChange(entry, oldPath, newPath, path);
//
//                if(data == null) {
//                    continue;
//                }
//            } else {

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

//                if(data == null) {
//                    continue;
//                }
//            }


            systemChange.getChanges().add(new Delta(oldPath, newPath, changeType, data));
        }

        String filePath = "./output/Delta_" + commitOld.substring(0, 4) + "_" + commitNew.substring(0, 4) + ".json";

        // Output the system changes
        JsonReadWriteUtils.writeToJSON(filePath, systemChange);

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
     * This method parses modified files and handles additional logic related to orphan management
     *
     * [!] Note: Due to the management of orphans we will do special checks here.
     *
     * @param path the file path that will be parsed
     * @return
     */
//    private JsonObject modify(String oldPath) {
//        JsonObject jsonObject = null;
//        JClass jClass = null;
//
//        if(FileUtils.isConfigurationFile(oldPath)) {
//            // Special check for modifying a pom that was previously filtered out for being too general
//            if(oldPath.endsWith("pom.xml") && oldSystem.findFile(oldPath) == null) {
//                return null;
//            }
//            jsonObject = add(oldPath);
//        } else {
//            jClass = SourceToObjectUtils.parseClass(new File(FileUtils.gitPathToLocalPath(oldPath, config.getRepoName())), config, "");
//        }
//
//        if(jClass != null) {
//            jsonObject = jClass.toJsonObject();
//        }
//
//        // Similar to add check, but if we couldn't parse and the class exists in old system we must allow it
//        if (jsonObject == null && oldSystem.findFile(oldPath) == null) {
//            return null;
//            // If the class is unparsable and the class exists in the old system we must delete it now
//        } else if (jsonObject == null && oldSystem.findFile(oldPath) != null) {
//            changeType = ChangeType.DELETE;
//            return delete();
//            // If the class is parsable and the class doesn't exist in the old system we must add it now
//        } else if (jsonObject != null && oldSystem.findFile(oldPath) == null) {
//            changeType = ChangeType.ADD;
//            return jsonObject;
//        } else if(jsonObject == null) {
//            System.err.println("No file found for " + oldPath);
//            System.exit(1);
//        }
//
//
//
//        return jsonObject;
//    }

    /**
     * This method returns a blank JsonObject() as there is no data to parse
     *
     * @return JsonObject that is empty
     */
    private JsonObject delete() {
        return new JsonObject();
    }




}
