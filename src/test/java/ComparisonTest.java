import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.ir.ProjectFile;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import org.junit.Test;


import java.util.HashSet;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class ComparisonTest {


    @Test
    public void testIR() {
        MicroserviceSystem ms1 = JsonReadWriteUtils.readFromJSON("./output/OldIR.json", MicroserviceSystem.class);
        MicroserviceSystem ms2 = JsonReadWriteUtils.readFromJSON("./output/IRCompare.json", MicroserviceSystem.class);

        deepCompareSystems(ms1, ms2);
    }

    private static void deepCompareSystems(MicroserviceSystem microserviceSystem1, MicroserviceSystem microserviceSystem2) {

        System.out.println("System equivalence is: " + Objects.deepEquals(microserviceSystem1, microserviceSystem2));

        for (Microservice microservice1 : microserviceSystem1.getMicroservices()) {
            outer2: {
                for (Microservice microservice2 : microserviceSystem2.getMicroservices()) {
                    if (microservice1.getName().equals(microservice2.getName())) {
                        System.out.println("Microservice equivalence of " + microservice1.getPath() + " is: " + Objects.deepEquals(microservice1, microservice2));
                        for (ProjectFile projectFile1 : microservice1.getProjectFiles()) {
                            outer1: {
                                for (ProjectFile projectFile2 : microservice2.getProjectFiles()) {
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
