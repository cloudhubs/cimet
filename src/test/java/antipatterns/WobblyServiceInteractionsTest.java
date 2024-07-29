package antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.WobblyServiceInteraction;
import edu.university.ecs.lab.detection.antipatterns.services.WobblyServiceInteractionService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

public class WobblyServiceInteractionsTest {
    private WobblyServiceInteractionService wobblyServiceInteractionService;
    private MicroserviceSystem microserviceSystem;

    @Before
    public void setUp(){
        FileUtils.makeDirs();

        IRExtractionService irExtractionService = new IRExtractionService("./test_config.json");

        irExtractionService.generateIR("TestIR.json");

        microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);

        wobblyServiceInteractionService = new WobblyServiceInteractionService();
    }

    @Test
    public void testWobblyServiceDetection(){
        WobblyServiceInteraction wobblyServiceInteraction = wobblyServiceInteractionService.findWobblyServiceInteractions(microserviceSystem);

        assertTrue(wobblyServiceInteraction.numWobbblyService() > 0);

        List<String> expectedWobblyServiceInt = new ArrayList<>(Arrays.asList("microservice-a.ServiceA.callServiceC"));

        assertTrue(wobblyServiceInteraction.getWobblyServiceInteractions().equals(expectedWobblyServiceInt));
    }
}
