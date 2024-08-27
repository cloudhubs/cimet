package unit.antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.ecs.lab.detection.antipatterns.services.ServiceChainMSLevelService;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import unit.Constants;

public class ServiceChainTest {
    private ServiceChainMSLevelService serviceChainService;
    private ServiceDependencyGraph sdg;

    @Before
    public void setUp(){
        FileUtils.makeDirs();

        IRExtractionService irExtractionService = new IRExtractionService(Constants.TEST_CONFIG_PATH, Optional.empty());

        irExtractionService.generateIR("TestIR.json");

        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);

        sdg = new ServiceDependencyGraph(microserviceSystem);

        serviceChainService = new ServiceChainMSLevelService();
    }

//    @Test
//    public void testServiceChainDetection(){
//        ServiceChain serviceChain = serviceChainService.getServiceChains(sdg);
//
//        assertTrue(serviceChain.numServiceChains() > 0);
//
//        List<List<String>> expectedServiceChain = List.of(
//            Arrays.asList("microservice-a", "microservice-b", "microservice-d"));
//
//        assertTrue(Objects.equals(serviceChain.getChain(), expectedServiceChain));
//    }

    @Test
    public void testNoServiceChains() {
        ServiceChainMSLevelService highThresholdServiceChain = new ServiceChainMSLevelService(5);

        ServiceChain serviceChain = highThresholdServiceChain.getServiceChains(sdg);

        assertNotNull(serviceChain);
        assertTrue(serviceChain.isEmpty());
        assertEquals(0, serviceChain.numServiceChains());
    }

     @Test
    public void testEmptyGraph() {
        ServiceDependencyGraph emptyGraph = new ServiceDependencyGraph(new MicroserviceSystem("EmptySystem", "baseCommit", new HashSet<>(), new HashSet<>()));

        ServiceChain serviceChain = serviceChainService.getServiceChains(emptyGraph);

        assertNotNull(serviceChain);
        assertTrue(serviceChain.isEmpty());
        assertEquals(0, serviceChain.numServiceChains());
    }

    @Test
    public void testNullGraph() {
        assertThrows(NullPointerException.class, () -> serviceChainService.getServiceChains(null));
    }
}
