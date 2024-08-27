package integration;

import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.ir.ProjectFile;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import org.junit.Test;


import java.util.*;

import static integration.Constants.TEST_CONFIG_PATH;
import static integration.Constants.TEST_IR_NAME;
import static org.junit.Assert.assertTrue;

public class ComparisonTest {


    @Test
    public void testIR() {
        MicroserviceSystem ms1 = JsonReadWriteUtils.readFromJSON("./output/NewIR.json", MicroserviceSystem.class);

        IRExtractionService irExtractionService = new IRExtractionService(TEST_CONFIG_PATH, Optional.empty());
        irExtractionService.generateIR(TEST_IR_NAME);

        MicroserviceSystem ms2 = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);
        ms1.setOrphans(null);
        ms2.setOrphans(null);
        deepCompareSystems(ms1, ms2);
        assertTrue(ms1.equals(ms2));
    }

    private static void deepCompareSystems(MicroserviceSystem microserviceSystem1, MicroserviceSystem microserviceSystem2) {

        System.out.println("System equivalence is: " + Objects.deepEquals(microserviceSystem1, microserviceSystem2));

        for (Microservice microservice1 : microserviceSystem1.getMicroservices()) {
            outer2: {
                for (Microservice microservice2 : microserviceSystem2.getMicroservices()) {
                    if (microservice1.getName().equals(microservice2.getName())) {
                        System.out.println("Microservice equivalence of " + microservice1.getPath() + " is: " + Objects.equals(microservice1, microservice2));
                        for (ProjectFile projectFile1 : microservice1.getAllFiles()) {
                            outer1: {
                                for (ProjectFile projectFile2 : microservice2.getAllFiles()) {
                                    if (projectFile1.getPath().equals(projectFile2.getPath())) {
                                        if(!Objects.equals(projectFile1, projectFile2)) {
                                            findDifferences(((JClass) projectFile1).getMethodCalls(), ((JClass) projectFile2).getMethodCalls());
                                            System.out.println("");
                                        }
                                        System.out.println("Class equivalence of " + projectFile1.getPath() + " is: " + Objects.equals(projectFile1, projectFile2));
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

    public static <T> Map<String, Set<T>> findDifferences(Set<T> set1, Set<T> set2) {
        Set<T> inFirstNotInSecond = new HashSet<>(set1);
        inFirstNotInSecond.removeAll(set2); // Elements in set1 but not in set2

        Set<T> inSecondNotInFirst = new HashSet<>(set2);
        inSecondNotInFirst.removeAll(set1); // Elements in set2 but not in set1

        Map<String, Set<T>> result = new HashMap<>();
        result.put("InFirstNotInSecond", inFirstNotInSecond);
        result.put("InSecondNotInFirst", inSecondNotInFirst);

        return result;
    }
}
