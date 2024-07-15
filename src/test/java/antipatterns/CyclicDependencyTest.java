package antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency;
import edu.university.ecs.lab.detection.antipatterns.services.CyclicDependencyService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

public class CyclicDependencyTest {
    private CyclicDependencyService cyclicService;
    private ServiceDependencyGraph sdg;

    @Before
    public void setUp(){
        Config config = ConfigUtil.readConfig("./test_config.json");

        FileUtils.createPaths();

        IRExtractionService irExtractionService = new IRExtractionService(config);

        irExtractionService.generateIR("TestIR.json");

        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);

        sdg = new ServiceDependencyGraph(microserviceSystem);

        cyclicService = new CyclicDependencyService();
    }

    @Test
    public void testCyclicDependencyDetection(){
        CyclicDependency cyclicDep = cyclicService.findCyclicDependencies(sdg);

        assertTrue(cyclicDep.numCyclicDep() > 0);

        List<List<String>> expectedCyclicDep = List.of(
                Arrays.asList("microservice-b", "microservice-d", "microservice-b"),
                Arrays.asList("microservice-a", "microservice-c", "microservice-a"));

        assertTrue(Objects.equals(cyclicDep.getCycles(), expectedCyclicDep));
    }
}
