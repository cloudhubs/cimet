package edu.university.ecs.lab.intermediate.merge.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.services.LoggerManager;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import java.nio.file.Path;
import java.util.*;


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

        // If no changes are present we will write back out same IR
        if (Objects.isNull(systemChange.getChanges())) {
            LoggerManager.debug(() -> "No changes found at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());
            JsonReadWriteUtils.writeToJSON("./output/NewIR.json", microserviceSystem);
            return;
        }


        // First we make necessary changes to microservices
        updateMicroservices();

        for (Delta d : systemChange.getChanges()) {

            switch (d.getChangeType()) {
                case ADD:
                    addFile(d);
                    break;
                case MODIFY:
                    removeFile(d);
                    addFile(d);
                    break;
                case DELETE:
                    removeFile(d);
                    break;
            }
        }

        microserviceSystem.setCommitID(systemChange.getNewCommit());

        LoggerManager.info(() -> "Merged to new IR at " + systemChange.getNewCommit());
        JsonReadWriteUtils.writeToJSON("./output/NewIR.json", microserviceSystem);
    }


    /**
     * This method adds a JClass based on a Delta change
     *
     * @param delta the delta change for adding
     */
    public void addFile(Delta delta) {
        // Check for unparsable files
        if(delta.getClassChange() == null && delta.getConfigChange() == null) {
            LoggerManager.warn(() -> "An added file has no change information " + delta.getNewPath());
            return;
        }

        Microservice ms = microserviceSystem.findMicroserviceByPath(delta.getNewPath());

        // If no ms is found, it will be held in orphans
        if (Objects.isNull(ms)) {
            if(delta.getClassChange() != null) {
                microserviceSystem.getOrphans().add(delta.getClassChange());
            } else if(delta.getConfigChange() != null) {
                microserviceSystem.getOrphans().add(delta.getConfigChange());
            }

            LoggerManager.debug(() -> "[File added] " + delta.getNewPath() + " to orphans at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());
            return;
        }

        // If we found it's ms
        if(delta.getConfigChange() != null) {
            ms.getFiles().add(delta.getConfigChange());
        } else {
            // Add the JClass, the microservice name is updated see addJClass()
            ms.addJClass(delta.getClassChange());
        }

        LoggerManager.debug(() -> "[File added] " + delta.getNewPath() + " to microservice " + ms.getPath() + " at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());


    }

    /**
     * This method removes a JClass based on a Delta change
     * Note it might not be found, so it will handle this gracefully
     *
     * @param delta the delta change for removal
     */
    public void removeFile(Delta delta) {
        Microservice ms = microserviceSystem.findMicroserviceByPath(delta.getOldPath());

        // If we are removing a file and it's microservice doesn't exist
        if (Objects.isNull(ms)) {
            // Check the orphan pool
            for (ProjectFile orphan : microserviceSystem.getOrphans()) {
                // If found remove it and return
                if (orphan.getPath().equals(delta.getOldPath())) {
                    microserviceSystem.getOrphans().remove(orphan);
                    LoggerManager.debug(() -> "[File removed] " + delta.getOldPath() + " from orphans at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());
                    return;
                }
            }
            LoggerManager.debug(() -> "[File not found] " + delta.getOldPath() + " in orphans at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());

            return;
        }

        // Remove the file depending on which is null, skips gracefully if not found in microservice
        // see removeProjectFile()
        // see removeProjectFile()
        ms.removeProjectFile(delta.getOldPath());

        LoggerManager.debug(() -> "[File removed] " + delta.getNewPath() + " from microservice " + ms.getPath() + " at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());


    }


    /**
     * Method for updating MicroserviceSystem structure (microservices) based on
     * pom.xml changes in Delta file
     *
     */
    private void updateMicroservices() {

        // See filterBuildDeltas()
        List<Delta> buildDeltas = filterBuildDeltas();

        if(buildDeltas.isEmpty()) {
            return;
        }

        // Loop through changes to pom.xml files
        for (Delta delta : buildDeltas) {


            Microservice microservice;
            String[] tokens;

            String path = delta.getOldPath().equals("/dev/null") ? delta.getNewPath() : delta.getOldPath();
            tokens = path.split("/");

            // Skip a pom that is in the root
            if (tokens.length <= 2) {
                LoggerManager.debug(() -> "Tokens check still needed?");
            }

            match: {
                switch (delta.getChangeType()) {
                    case ADD:

                        // If a delta is more/less specific than an active microservice
                        Microservice removeMicroservice = null;
                        for (Microservice compareMicroservice : microserviceSystem.getMicroservices()) {
                            // If delta is more specific than compareMicroservice, we remove this one
                            if (delta.getNewPath().replace("/pom.xml", "").startsWith(compareMicroservice.getPath())) {
                                removeMicroservice = compareMicroservice;

                            // If a microservice already exists that is more specific, skip the addition
                            } else if (compareMicroservice.getPath().startsWith(delta.getNewPath().replace("/pom.xml", ""))) {
                                break match;
                            }
                        }

                        // If a match was found, orphanize and remove. They will be adopted below
                        if (Objects.nonNull(removeMicroservice)) {
                            microserviceSystem.getMicroservices().remove(removeMicroservice);
                            microserviceSystem.orphanize(removeMicroservice);
                        }

                        microservice = new Microservice(tokens[tokens.length - 2], delta.getNewPath().replace("/pom.xml", ""));
                        // Here we must check if any orphans are waiting on this creation
                        microserviceSystem.adopt(microservice);
                        microserviceSystem.getMicroservices().add(microservice);
                        LoggerManager.debug(() -> "[Microservice added]  " + microservice.getName() + " " + microservice.getPath() + " at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());
                        break;


                    case DELETE:
                        microservice = microserviceSystem.findMicroserviceByPath(delta.getOldPath().replace("/pom.xml", ""));

                        // TODO REMOVE
                        if (microservice == null) {
                            LoggerManager.error(() -> "[Microservice not found]  " + delta.getOldPath() + " at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit(), Optional.of(new RuntimeException("Fail")));
                        }

                        // Here we must orphan all the classes of this microservice
                        microserviceSystem.getMicroservices().remove(microservice);
                        microserviceSystem.orphanize(microservice);
                        LoggerManager.debug(() -> "[Microservice removed]  " + microservice.getName() + " " + microservice.getPath() + " at " + systemChange.getOldCommit() + " -> " + systemChange.getNewCommit());
                        break;

                }
            }

        }


    }

    /**
     * Filter's the delta files that deal with building project
     * so either pom.xml or build.gradle
     *
     * @return a list of system changes that deal with build files that aren't modifications
     */
    private List<Delta> filterBuildDeltas() {
        // deltaChanges.stream().filter(delta -> (delta.getOldPath() == null || delta.getOldPath().isEmpty() ? delta.getNewPath() : delta.getOldPath()).endsWith("/pom.xml")).collect(Collectors.toUnmodifiableList());
        List<Delta> filteredDeltas = new ArrayList<>(systemChange.getChanges());

        // Remove non build related files
        filteredDeltas.removeIf(delta -> !(delta.getOldPath().endsWith("/pom.xml") || delta.getNewPath().endsWith("/pom.xml")) && !(delta.getOldPath().endsWith("/build.gradle") || delta.getNewPath().endsWith("/build.gradle")));

        // Remove modified files, doesn't change microservice structure
        filteredDeltas.removeIf(delta -> delta.getChangeType().equals(ChangeType.MODIFY));

        // Remove more specific build deltas in the same system change
        List<Delta> filteredDeltasCopy = new ArrayList<>(List.copyOf(filteredDeltas));


        // If a delta is more specific than another in same SystemChange,
        // we need to remove the more general option in case of add
        for(Delta delta1 : filteredDeltas) {
            for(Delta delta2 : filteredDeltas) {
                // If they are equal or they aren't both additions
                if(delta1.equals(delta2) || !delta1.getChangeType().equals(ChangeType.ADD) || !delta2.getChangeType().equals(ChangeType.ADD)) {
                    continue;
                }

                // startsWith is usable since the implication is the path is longer than just the match
                // seeing as it isn't possible to have two deltas with the same newPath
                if(delta1.getNewPath().replace("/pom.xml", "").startsWith(delta2.getNewPath().replace("/pom.xml", ""))) {
                    filteredDeltasCopy.remove(delta1);
                } else if(delta2.getNewPath().replace("/pom.xml", "").startsWith(delta1.getNewPath().replace("/pom.xml", ""))) {
                    filteredDeltasCopy.remove(delta2);
                }
            }
        }


        return filteredDeltasCopy;
    }

    private String getMicroserviceNameFromPath(String path) {
        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            if (path.contains(microservice.getPath())) {
                return microservice.getName();
            }
        }

        return null;
    }


}
