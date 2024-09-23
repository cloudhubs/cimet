//package integration;
//
//import edu.university.ecs.lab.common.models.ir.*;
//import edu.university.ecs.lab.common.services.GitService;
//import edu.university.ecs.lab.common.utils.FileUtils;
//import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
//import edu.university.ecs.lab.delta.services.DeltaExtractionService;
//import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
//import edu.university.ecs.lab.intermediate.merge.services.MergeService;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.*;
//
//import static integration.Constants.*;
//
//class IRComparisonTest {
//    private static IRExtractionService irExtractionService;
//    private static DeltaExtractionService deltaExtractionService;
//    private static List<RevCommit> list;
//    private static GitService gitService;
//
//
//    @BeforeAll
//    public static void setUp() {
//        FileUtils.makeDirs();
//        gitService = new GitService(TEST_CONFIG_PATH);
//
//        list = iterableToList(gitService.getLog());
//
//        irExtractionService = new IRExtractionService(TEST_CONFIG_PATH, Optional.of(list.get(0).toString().split(" ")[1]));
//
//        irExtractionService.generateIR(OLD_IR_NAME);
//    }
//
//    @Test
//    void testComparison() {
//
//        // Loop through commit history and create delta, merge, etc...
//        for (int i = 0; i < list.size() - 1; i++) {
//            String commitIdOld = list.get(i).toString().split(" ")[1];
//            String commitIdNew = list.get(i + 1).toString().split(" ")[1];
//
//            // Extract changes from one commit to the other
//            deltaExtractionService = new DeltaExtractionService(TEST_CONFIG_PATH, OLD_IR_PATH, commitIdOld, commitIdNew);
//            deltaExtractionService.generateDelta();
//
//            // Merge Delta changes to old IR to create new IR representing new commit changes
//            MergeService mergeService = new MergeService(OLD_IR_PATH, DELTA_PATH, TEST_CONFIG_PATH);
//            mergeService.generateMergeIR(commitIdNew);
//
//            if(i < list.size() - 2) {
//                try {
//                    Files.move(Paths.get(NEW_IR_PATH), Paths.get(OLD_IR_PATH), StandardCopyOption.REPLACE_EXISTING);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        // Create IR of last commit
//        irExtractionService = new IRExtractionService(TEST_CONFIG_PATH, Optional.of(list.get(list.size() - 1).toString().split(" ")[1]));
//        irExtractionService.generateIR(TEST_IR_NAME);
//
//        // Compare two IR's for equivalence
//        MicroserviceSystem microserviceSystem1 = JsonReadWriteUtils.readFromJSON(NEW_IR_PATH, MicroserviceSystem.class);
//        MicroserviceSystem microserviceSystem2 = JsonReadWriteUtils.readFromJSON(TEST_IR_PATH, MicroserviceSystem.class);
//
//        microserviceSystem1.setOrphans(new HashSet<>());
//        microserviceSystem2.setOrphans(new HashSet<>());
//
//        Assertions.assertTrue(Objects.deepEquals(microserviceSystem1, microserviceSystem2));
//
//    }
//
//
////    private static void deepCompareSystems(MicroserviceSystem microserviceSystem1, MicroserviceSystem microserviceSystem2) {
////        // Ignore orphans for testing
////        microserviceSystem1.setOrphans(null);
////        microserviceSystem2.setOrphans(null);
////        System.out.println("System equivalence is: " + Objects.deepEquals(microserviceSystem1, microserviceSystem2));
////
////        for (Microservice microservice1 : microserviceSystem1.getMicroservices()) {
////            outer2: {
////                for (Microservice microservice2 : microserviceSystem2.getMicroservices()) {
////                    if (microservice1.getName().equals(microservice2.getName())) {
////                        System.out.println("Microservice equivalence of " + microservice1.getPath() + " is: " + Objects.deepEquals(microservice1, microservice2));
////                        for (ProjectFile projectFile1 : microservice1.getAllFiles()) {
////                            outer1: {
////                                for (ProjectFile projectFile2 : microservice2.getAllFiles()) {
////                                    if (projectFile1.getPath().equals(projectFile2.getPath())) {
////                                        System.out.println("Class equivalence of " + projectFile1.getPath() + " is: " + Objects.deepEquals(projectFile1, projectFile2));
////                                        break outer1;
////                                    }
////                                }
////
////                                System.out.println("No JClass match found for " + projectFile1.getPath());
////                            }
////                        }
////                        break outer2;
////                    }
////                }
////
////                System.out.println("No Microservice match found for " + microservice1.getPath());
////            }
////        }
////
////    }
//
//    private static List<RevCommit> iterableToList(Iterable<RevCommit> iterable) {
//        Iterator<RevCommit> iterator = iterable.iterator();
//        List<RevCommit> list = new LinkedList<>();
//        while (iterator.hasNext()) {
//            list.add(iterator.next());
//        }
//        Collections.reverse(list);
//
//        return list;
//    }
//
//
//}
