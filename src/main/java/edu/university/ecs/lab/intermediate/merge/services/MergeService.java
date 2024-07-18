package edu.university.ecs.lab.intermediate.merge.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class is used for creating new IR's from old IR + Delta
 * and provides all functionality related to updating the old
 * IR
 */
public class MergeService {
    private final Config config;
    private final MicroserviceSystem microserviceSystem;
    private final SystemChange systemChange;

    // TODO handle exceptions here
    public MergeService(
            String intermediatePath,
            String deltaPath,
            String configPath) {
        this.config = ConfigUtil.readConfig(configPath);
        this.microserviceSystem = JsonReadWriteUtils.readFromJSON(Path.of(intermediatePath).toAbsolutePath().toString(), MicroserviceSystem.class);
        this.systemChange = JsonReadWriteUtils.readFromJSON(Path.of(deltaPath).toAbsolutePath().toString(), SystemChange.class);
    }

    /**
     * This method generates the new IR from the old IR + Delta file
     */
    public void generateMergeIR() {
        System.out.println("Merging to new IR!");

        // If no changes are present we will write back out same IR
        if (Objects.isNull(systemChange.getChanges())) {
            JsonReadWriteUtils.writeToJSON("./output/NewIR.json", microserviceSystem);
            return;
        }


        // First we make necessary changes to microservices
        updateMicroservices(systemChange.getChanges());

        for (Delta d : systemChange.getChanges()) {

            // Check for pom.xml
//            if (!path.endsWith(".java")) {
//                continue;
//            }

            switch (d.getChangeType()) {
                case ADD:
                    addFile(d);
                    break;
                case MODIFY:
                    modifyFiles(d);
                    break;
                case DELETE:
                    removeFile(d);
                    break;
            }
        }

        microserviceSystem.setCommitID(systemChange.getNewCommit());
        JsonReadWriteUtils.writeToJSON("./output/NewIR.json", microserviceSystem);
    }


    /**
     * This method modifies a JClass based on a Delta change
     *
     * @param delta the delta change for modifying
     */
    public void modifyFiles(Delta delta) {
        // Here the path is irrelevant since it does not change
        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(getMicroserviceNameFromPath(delta.getOldPath()))).findFirst().orElse(null);

        // If we dont find a microservice
        if (Objects.isNull(ms)) {
            // Check the orphan pool
            for (ProjectFile orphan : microserviceSystem.getOrphans()) {
                // If found remove it and return
                if (orphan.getPath().equals(delta.getOldPath())) {
                    microserviceSystem.getOrphans().remove(orphan);

                    // Only add it back if we parsed a valid JClass (not null)
                    if (delta.getClassChange() != null) {
                        microserviceSystem.getOrphans().add(delta.getClassChange());
                    } else if(delta.getConfigChange() != null) {
                        microserviceSystem.getOrphans().add(delta.getConfigChange());
                    }

                    return;
                }
            }
            return;
        }

        Set<JClass> classes = ms.getClasses();

        for (JClass jClass : classes) {
            if (jClass.getPath().equals(delta.getOldPath())) {
                ms.removeJClass(delta.getOldPath());

                // Only add it back if we parsed a valid JClass (not null)
                if (delta.getClassChange() != null) {
                    JClass jClass1 = delta.getClassChange();
                    jClass1.updateMicroserviceName(getMicroserviceNameFromPath(delta.getNewPath()));
                    ms.addJClass(jClass1);
                    ms.addJClass(jClass1);
                }

                return;
            }
        }

        // If we modify a class that was previously invalid
        // and we dont find it in previous classes or orphans
        // we should still add it because it might have been invalid
        // when we first tried to add it and was dropped
        if (delta.getClassChange() != null) {
            JClass jClass = delta.getClassChange();
            jClass.updateMicroserviceName(getMicroserviceNameFromPath(delta.getNewPath()));
            ms.addJClass(jClass);
            ms.addJClass(jClass);
        }

    }

    /**
     * This method adds a JClass based on a Delta change
     *
     * @param delta the delta change for adding
     */
    public void addFile(Delta delta) {
        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(getMicroserviceNameFromPath(delta.getNewPath()))).findFirst().orElse(null);

        // If we cant find his microservice after we called updateMicroservices then a file was pushed without a pom.xml
        // so it will be held as an orphan
        if (Objects.isNull(ms)) {
            if(delta.getClassChange() != null) {
                microserviceSystem.getOrphans().add(delta.getClassChange());
            } else if(delta.getConfigChange() != null) {
                microserviceSystem.getOrphans().add(delta.getConfigChange());
            }

            return;
        }

        if(FileUtils.isConfigurationFile(delta.getNewPath())) {
            ms.getFiles().add(delta.getConfigChange());
        } else {
            // Update microservice name if we are a new class, after pom manipulations
            JClass jClass = delta.getClassChange();
            jClass.updateMicroserviceName(getMicroserviceNameFromPath(delta.getNewPath()));
            ms.addJClass(jClass);

        }


    }

    /**
     * This method removes a JClass based on a Delta change
     *
     * @param delta the delta change for removal
     */
    public void removeFile(Delta delta) {
        Microservice ms = microserviceSystem.getMicroservices().stream().filter(microservice -> microservice.getName().equals(getMicroserviceNameFromPath(delta.getOldPath()))).findFirst().orElse(null);

        // If we are removing a file and it's microservice doesn't exist
        if (Objects.isNull(ms)) {
            // Check the orphan pool
            for (ProjectFile orphan : microserviceSystem.getOrphans()) {
                // If found remove it and return
                if (orphan.getPath().equals(delta.getOldPath())) {
                    microserviceSystem.getOrphans().remove(orphan);
                    return;
                }
            }
            return;
        }

        if(FileUtils.isConfigurationFile(delta.getOldPath())) {
            ConfigFile deleteFile = null;

            for (ConfigFile configFile : ms.getFiles()) {
                if (configFile.getPath().equals(delta.getOldPath())) {
                    deleteFile = configFile;
                    break;
                }
            }
            ms.getFiles().remove(deleteFile);

        } else {
            ms.removeJClass(delta.getOldPath());

        }


    }


    /**
     * Method for updating MicroserviceSystem structure (microservices) based on
     * pom.xml changes in Delta file
     *
     * @param deltaChanges the delta changes to search
     */
    private void updateMicroservices(List<Delta> deltaChanges) {

        // Only get pom deltas
        List<Delta> pomDeltas = deltaChanges.stream().filter(delta -> (delta.getOldPath() == null || delta.getOldPath().isEmpty() ? delta.getNewPath() : delta.getOldPath()).endsWith("/pom.xml")).collect(Collectors.toUnmodifiableList());

        // Loop through changes to pom.xml files
        for (Delta delta : pomDeltas) {

            Microservice microservice;
            String[] tokens;

            String path = delta.getOldPath() == null ? delta.getNewPath() : delta.getOldPath();
            tokens = path.split("/");

            // Skip a pom that is in the root
            if (tokens.length <= 2) {
                continue;
            }

            switch (delta.getChangeType()) {
                case ADD:
                    microservice = new Microservice(tokens[tokens.length - 2], delta.getNewPath().replace("/pom.xml", ""));
                    // Here we must check if any orphans are waiting on this creation
                    microserviceSystem.adopt(microservice);
                    microserviceSystem.getMicroservices().add(microservice);
                    break;
                case DELETE:
                    microservice = microserviceSystem.findMicroserviceByPath(delta.getOldPath().replace("/pom.xml", ""));
                    // Here we must orphan all the classes of this microservice
                    if(microservice == null) {
                        System.out.println(delta.getOldPath() + " not found");
                    }
                    microserviceSystem.orphanize(microservice);
                    microserviceSystem.getMicroservices().removeIf(ms -> ms.getPath().equals(microservice.getPath()));
                    break;

            }

        }


    }

    private String getMicroserviceNameFromPath(String path) {
        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            if (path.contains(microservice.getPath())) {
                return microservice.getName();
            }
        }

        return null;
    }

//    private void orphanizeAndAdopt(Microservice microservice) {
//        microserviceSystem.orphanize(microservice);
//        microserviceSystem.getMicroservices().remove(microservice);
//
//        for(Microservice microservice1 : microserviceSystem.getMicroservices()) {
//            microserviceSystem.adopt(microservice1);
//        }
//    }
}
