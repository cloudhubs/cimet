package integration;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class IRComparisonTest {

    public static void main(String[] args) {
        final String CONFIG_PATH = "./config.json";
        Config config = ConfigUtil.readConfig(CONFIG_PATH);
        DeltaExtractionService deltaExtractionService;
        FileUtils.makeDirs();
        GitService gitService = new GitService(CONFIG_PATH);

        Iterable<RevCommit> commits = gitService.getLog();

        Iterator<RevCommit> iterator = commits.iterator();
        List<RevCommit> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.reverse(list);
        // Create IR of first commit
        createIRSystem(CONFIG_PATH, "OldIR.json", list.get(0).toString().split(" ")[1]);


        // Loop through commit history and create delta, merge, etc...
        for (int i = 0; i < list.size() - 1; i++) {
            String commitIdOld = list.get(i).toString().split(" ")[1];
            String commitIdNew = list.get(i + 1).toString().split(" ")[1];

            // Extract changes from one commit to the other
            deltaExtractionService = new DeltaExtractionService("./config.json", "./output/OldIR.json", commitIdOld, commitIdNew);
            deltaExtractionService.generateDelta();

            // Merge Delta changes to old IR to create new IR representing new commit changes
            MergeService mergeService = new MergeService("./output/OldIR.json", "./output/Delta.json", "./config.json");
            mergeService.generateMergeIR();

            try {
                Files.move(Paths.get("./output/NewIR.json"), Paths.get("./output/OldIR.json"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //computeGraph("./output/rest-extraction-output-[main-" + commitIdNew.substring(0,7) + "].json", commitIdNew.substring(0,7));
        }

        // Create IR of last commit
        createIRSystem(CONFIG_PATH, "IRCompare.json", list.get(list.size() - 1).toString().split(" ")[1]);

        // Compare two IR's for equivalence
        MicroserviceSystem microserviceSystem1 = JsonReadWriteUtils.readFromJSON("./output/OldIR.json", MicroserviceSystem.class);
        microserviceSystem1.setCommitID(list.get(list.size() - 1).toString().split(" ")[1]);
        // Clear orphans
        microserviceSystem1.setOrphans(new HashSet<>());

        MicroserviceSystem microserviceSystem2 = JsonReadWriteUtils.readFromJSON("./output/IRCompare.json", MicroserviceSystem.class);

        deepCompareSystems(microserviceSystem1, microserviceSystem2);


        // Output results
//        System.out.println(b);

    }

    private static void createIRSystem(String configPath, String fileName, String commitID) {
        // Create both directories needed
        FileUtils.makeDirs();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(configPath, Optional.of(commitID));

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }


    private static void deepCompareSystems(MicroserviceSystem microserviceSystem1, MicroserviceSystem microserviceSystem2) {
        // Ignore orphans for testing
        microserviceSystem1.setOrphans(null);
        microserviceSystem2.setOrphans(null);
        System.out.println("System equivalence is: " + Objects.deepEquals(microserviceSystem1, microserviceSystem2));

        for (Microservice microservice1 : microserviceSystem1.getMicroservices()) {
            outer2: {
                for (Microservice microservice2 : microserviceSystem2.getMicroservices()) {
                    if (microservice1.getName().equals(microservice2.getName())) {
                        System.out.println("Microservice equivalence of " + microservice1.getPath() + " is: " + Objects.deepEquals(microservice1, microservice2));
                        for (ProjectFile projectFile1 : microservice1.getAllFiles()) {
                            outer1: {
                                for (ProjectFile projectFile2 : microservice2.getAllFiles()) {
                                    if (projectFile1.getPath().equals(projectFile2.getPath())) {
                                        System.out.println("Class equivalence of " + projectFile1.getPath() + " is: " + Objects.deepEquals(projectFile1, projectFile2));
                                        break outer1;
                                    }
                                }

                                System.out.println("No JClass match found for " + projectFile1.getPath());
                            }
                        }
                        break outer2;
                    }
                }

                System.out.println("No Microservice match found for " + microservice1.getPath());
            }
        }

    }


}
