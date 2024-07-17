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

/**
 * Service for extracting the differences between two commits of a repository.
 * This class does cleaning of output so not all changes will be reflected in
 * the Delta output file.
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
     * The old IR for validating delta file changes
     */
    private final MicroserviceSystem oldSystem;

    /**
     * System change object that will be returned
     */
    private SystemChange systemChange;

    /**
     * Represents a list of diff entries that are related to pom.xml add or delete
     */
    private final List<DiffEntry> pomDiffs;

    /**
     * The type of change that is made
     */
    private ChangeType changeType;


    /**
     * Constructor for the DeltaExtractionService
     *
     * @param configPath path to the config file
     * @param commitOld old commit for comparison
     * @param commitNew new commit for comparison
     */
    public DeltaExtractionService(String configPath, String oldIRPath, String commitOld, String commitNew) {
        this.config = ConfigUtil.readConfig(configPath);
        this.gitService = new GitService(configPath);
        this.commitOld = commitOld;
        this.commitNew = commitNew;
        this.oldSystem = JsonReadWriteUtils.readFromJSON(oldIRPath, MicroserviceSystem.class);
        pomDiffs = new ArrayList<>();
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
                newPath = null;

            } else if (DiffEntry.ChangeType.ADD.equals(entry.getChangeType())) {
                oldPath = null;
                newPath = FileUtils.GIT_SEPARATOR + entry.getNewPath();

            } else {
                oldPath = FileUtils.GIT_SEPARATOR + entry.getOldPath();
                newPath = FileUtils.GIT_SEPARATOR + entry.getNewPath();

            }


            changeType = ChangeType.fromDiffEntry(entry);

            // Special check for pom file manipulations only
            if(path.endsWith("pom.xml")) {
                // Get some configuration change
                data = configurationChange(entry, newPath);

                if(data == null && !entry.getChangeType().equals(DiffEntry.ChangeType.DELETE)) {
                    continue;
                }
            } else {

                switch(changeType) {
                    case ADD:
                        data = add(newPath);
                        break;
                    case MODIFY:
                        data = modify(oldPath);
                        break;
                    case DELETE:
                        data = delete(oldPath);
                }

                if(data == null) {
                    continue;
                }
            }


            systemChange.getChanges().add(new Delta(oldPath, newPath, changeType, data));
        }

        // Output the system changes
        JsonReadWriteUtils.writeToJSON("./output/Delta.json", systemChange);

        // Report
        System.out.println("Delta extracted: from " + commitOld + " to " + commitNew + " at ./output/Delta.json");

    }

    /**
     * This method parses a newly added file
     *
     * @param path the file path that will be parsed
     * @return null if the JClass is unparsable or a data object representing a parsed JClass or config file
     */
    private JsonObject add(String newPath) {
        JsonObject jsonObject;
        if(FileUtils.isConfigurationFile(newPath)) {
            ConfigFile configFile = SourceToObjectUtils.parseConfigurationFile(new File(FileUtils.gitPathToLocalPath(newPath, config.getRepoName())), config);
            if(configFile == null || configFile.getData() == null) {
                return null;
            }
            jsonObject = configFile.toJsonObject();
        } else {
            JClass jClass = SourceToObjectUtils.parseClass(new File(FileUtils.gitPathToLocalPath(newPath, config.getRepoName())), config, "");
            if(jClass == null) {
                return null;
            }
            jsonObject = jClass.toJsonObject();
        }

        return jsonObject;
    }

    /**
     * This method parses modified files and handles additional logic related to orphan management
     *
     * [!] Note: Due to the management of orphans we will do special checks here.
     *
     * @param path the file path that will be parsed
     * @return
     */
    private JsonObject modify(String oldPath) {
        JsonObject jsonObject = null;
        JClass jClass = null;

        if(FileUtils.isConfigurationFile(oldPath)) {
            jsonObject = add(oldPath);
        } else {
            jClass = SourceToObjectUtils.parseClass(new File(FileUtils.gitPathToLocalPath(oldPath, config.getRepoName())), config, "");
        }

        if(jClass != null) {
            jsonObject = jClass.toJsonObject();
        }

        // Similar to add check, but if we couldn't parse and the class exists in old system we must allow it
        if (jsonObject == null && oldSystem.findFile(oldPath) == null) {
            return null;
            // If the class is unparsable and the class exists in the old system we must delete it now
        } else if (jsonObject == null && oldSystem.findFile(oldPath) != null) {
            changeType = ChangeType.DELETE;
            return delete(oldPath);
            // If the class is parsable and the class doesn't exist in the old system we must add it now
        } else if (jsonObject != null && oldSystem.findFile(oldPath) == null) {
            changeType = ChangeType.ADD;
            return jsonObject;
        } else if(jsonObject == null) {
            System.err.println("No file found for " + oldPath);
            System.exit(1);
        }



        return jsonObject;
    }

    /**
     * This method does no parsing but validates that the class to be deleted does exist
     *
     * @param oldPath the path before the change (new path is /dev/null)
     * @return null if it does not exist or empty data object if it does
     */
    private JsonObject delete(String oldPath) {
        return oldSystem.findFile(oldPath) == null ? null : new JsonObject();
    }

    /**
     * This method handles manipulations pom only and does additional logic regarding
     * additions/removals of microservices which is based around pom manipulation
     *
     * @param entry the diffentry representing the pom change
     * @return
     */
    private JsonObject configurationChange(DiffEntry entry, String newPath) {
        if(entry.getChangeType().equals(DiffEntry.ChangeType.DELETE)) {
            return null;
        }

        // Special manipulation for poms, they control creation/deletion of microservices
        // If we are modifying the pom, the microservice remains, let's just update the files for it
        if(!entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY)) {
            // If we add a pom
            if(entry.getChangeType().equals(DiffEntry.ChangeType.ADD)) {
                DiffEntry remove = null;

                // For all existing pomEntry's in this delta
                for (DiffEntry pomEntry : pomDiffs) {

                    // If we have an existing entry that is more specific than the current entry
                    if (pomEntry.getNewPath().replace("/pom.xml", "").startsWith(newPath.replace("/pom.xml", ""))) {
                        return null;

                        // If the current entry is more specific than an existing entry
                    } else if (newPath.replace("/pom.xml", "").startsWith(pomEntry.getNewPath().replace("/pom.xml", ""))) {
                        // Remove the old entry
                        remove = pomEntry;
                    }

                }


                // Check existing microservices if our entry is more specific as well
                for (Microservice microservice : oldSystem.getMicroservices()) {

                    // If we find a match, orphanize and redistribute those classes naturally
                    if (("/" + newPath.replace("/pom.xml", "")).startsWith(microservice.getPath())) {
                        oldSystem.orphanize(microservice);
                        oldSystem.getMicroservices().remove(microservice);
                        break;
                    }

                }


                // If we remove a pom diff, delete the delta entry that already passed
                if(remove != null) {
                    // Remove from current poms
                    pomDiffs.remove(remove);
                    // Remove delta entry
                    Iterator<Delta> deltaIter = systemChange.getChanges().iterator();
                    while (deltaIter.hasNext()) {
                        Delta d = deltaIter.next();
                        if((d.getOldPath() == null && remove.getOldPath().equals("/dev/null")
                                || d.getOldPath() != null && d.getOldPath().equals("/" + remove.getOldPath())) &&
                                (d.getNewPath() == null && remove.getNewPath().equals("/dev/null")
                                        || d.getNewPath() != null && d.getNewPath().equals("/" + remove.getNewPath()))) {
                            deltaIter.remove();
                            break;
                        }
                    }
                }

                // Add the current entry
                pomDiffs.add(entry);


                // If we are modifying or deleting an existing pom
            }

            // Update the oldSystem based on changes
            JsonReadWriteUtils.writeToJSON("./output/OldIR.json", oldSystem);
        }

        // Regardless some configuration file will be built for this pom
        ConfigFile configFile = SourceToObjectUtils.parseConfigurationFile(new File(FileUtils.gitPathToLocalPath(newPath, config.getRepoName())), config);

        // If we get
        if(configFile == null || configFile.getData() == null) {
            throw new RuntimeException("No configuration file found for " + newPath);
        }


        return configFile.toJsonObject();

    }


}
