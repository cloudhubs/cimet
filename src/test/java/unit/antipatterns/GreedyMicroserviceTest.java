package unit.antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.GreedyMicroservice;
import edu.university.ecs.lab.detection.antipatterns.services.GreedyService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import unit.Constants;

import java.util.*;

public class GreedyMicroserviceTest {
    private GreedyService greedyService;
    private ServiceDependencyGraph sdg;

    @Before
    public void setUp(){
        FileUtils.makeDirs();

        IRExtractionService irExtractionService = new IRExtractionService(Constants.TEST_CONFIG_PATH, Optional.empty());

        irExtractionService.generateIR("TestIR.json");

        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);

        sdg = new ServiceDependencyGraph(microserviceSystem);

        greedyService = new GreedyService(2);
    }

    @Test
    public void testGreedyMicroserviceDetection() {
        GreedyMicroservice greedyMicroservice = greedyService.getGreedyMicroservices(sdg);

        assertTrue(greedyMicroservice.numGreedyMicro() > 0);

        List<String> expectedGreedyMicroservices = new ArrayList<>(Arrays.asList("microservice-a"));

        assertTrue(greedyMicroservice.getGreedyMicroservices().equals(expectedGreedyMicroservices));

    }
    @Test
    public void testNoGreedyMicroservices() {
        GreedyService highThresholdGreedyService = new GreedyService(10);

        GreedyMicroservice greedyMicroservice = highThresholdGreedyService.getGreedyMicroservices(sdg);

        assertNotNull(greedyMicroservice);
        assertTrue(greedyMicroservice.isEmpty());
        assertEquals(0, greedyMicroservice.numGreedyMicro());
    }

    @Test
    public void testEmptyGraph() {
        ServiceDependencyGraph emptyGraph = new ServiceDependencyGraph(new MicroserviceSystem("EmptySystem", "baseCommit", new HashSet<>(), new HashSet<>()));

        GreedyMicroservice greedyMicroservice = greedyService.getGreedyMicroservices(emptyGraph);

        assertNotNull(greedyMicroservice);
        assertTrue(greedyMicroservice.isEmpty());
        assertEquals(0, greedyMicroservice.numGreedyMicro());
    }

    @Test
    public void testMultipleGreedyMicroservices() {
        GreedyService multiGreedyService = new GreedyService(1);

        GreedyMicroservice greedyMicroservice = multiGreedyService.getGreedyMicroservices(sdg);

        assertNotNull(greedyMicroservice);
        assertTrue(greedyMicroservice.numGreedyMicro() > 1);

        List<String> expectedGreedyMicroservices = new ArrayList<>(Arrays.asList("microservice-a", "microservice-b", "microservice-c", "microservice-d"));

        Collections.sort(greedyMicroservice.getGreedyMicroservices());

        assertTrue(greedyMicroservice.getGreedyMicroservices().equals(expectedGreedyMicroservices));
    }

    @Test
    public void testEdgeCaseWithExactThreshold() {
        GreedyService exactThresholdService = new GreedyService(2);

        GreedyMicroservice greedyMicroservice = exactThresholdService.getGreedyMicroservices(sdg);

        assertNotNull(greedyMicroservice);
        assertEquals(1, greedyMicroservice.numGreedyMicro());

        List<String> expectedGreedyMicroservices = new ArrayList<>(Arrays.asList("microservice-a"));

        assertTrue(greedyMicroservice.getGreedyMicroservices().equals(expectedGreedyMicroservices));
    }

    @Test
    public void testNullGraph() {
        assertThrows(NullPointerException.class, () -> greedyService.getGreedyMicroservices(null));
    }
}
