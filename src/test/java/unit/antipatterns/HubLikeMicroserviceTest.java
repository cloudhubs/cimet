package unit.antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.HubLikeMicroservice;
import edu.university.ecs.lab.detection.antipatterns.services.HubLikeService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import unit.Constants;


@Ignore
public class HubLikeMicroserviceTest {
    private HubLikeService hubLikeService;
    private ServiceDependencyGraph sdg;

    @Before
    public void setUp(){

        FileUtils.makeDirs();

        IRExtractionService irExtractionService = new IRExtractionService(Constants.TEST_CONFIG_PATH, Optional.empty());

        irExtractionService.generateIR("TestIR.json");

        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);

        sdg = new ServiceDependencyGraph(microserviceSystem);

        hubLikeService = new HubLikeService(2);
    }

    @Test
    public void testHubLikeMicroserviceDetection() {
        HubLikeMicroservice hubLikeMicroservice = hubLikeService.getHubLikeMicroservice(sdg);

        assertTrue(hubLikeMicroservice.numHubLike() > 0);

        List<String> expectedHubLikeMicroservices = new ArrayList<>(Arrays.asList("microservice-b"));

        assertTrue(hubLikeMicroservice.getHublikeMicroservices().equals(expectedHubLikeMicroservices));
    }

    @Test
    public void testNoHubLikeMicroservices(){
        HubLikeService highThresholdHubLikeService = new HubLikeService(10);

        HubLikeMicroservice hubLikeMicroservice = highThresholdHubLikeService.getHubLikeMicroservice(sdg);

        assertNotNull(hubLikeMicroservice);
        assertTrue(hubLikeMicroservice.isEmpty());
        assertEquals(0, hubLikeMicroservice.numHubLike());
    }

    @Test
    public void testMultipleHubLikeMicroservices(){
        HubLikeService multiHubLikeService = new HubLikeService(1);

        HubLikeMicroservice hubLikeMicroservice = multiHubLikeService.getHubLikeMicroservice(sdg);

        assertNotNull(hubLikeMicroservice);
        assertTrue(hubLikeMicroservice.numHubLike() > 1);

        List<String> expectedHubLikeMicroservices = new ArrayList<>(Arrays.asList("microservice-a", "microservice-b", "microservice-c", "microservice-d"));

        Collections.sort(hubLikeMicroservice.getHublikeMicroservices());

        assertTrue(hubLikeMicroservice.getHublikeMicroservices().equals(expectedHubLikeMicroservices));
    }

    @Test
    public void testEdgeCaseWithExactThreshold(){
        HubLikeService exactThresholdService = new HubLikeService(2);

        HubLikeMicroservice hubLikeMicroservice = exactThresholdService.getHubLikeMicroservice(sdg);

        assertNotNull(hubLikeMicroservice);
        assertEquals(1, hubLikeMicroservice.numHubLike());

        List<String> expectedHubLikeMicroservices = new ArrayList<>(Arrays.asList("microservice-b"));

        assertTrue(hubLikeMicroservice.getHublikeMicroservices().equals(expectedHubLikeMicroservices));
    }

    @Test
    public void testNullGraph(){
        assertThrows(NullPointerException.class, () -> hubLikeService.getHubLikeMicroservice(null));
    }
}
