package antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.WrongCuts;
import edu.university.ecs.lab.detection.antipatterns.services.WrongCutsService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

public class WrongCutsTest {
    private WrongCutsService wrongCutsService;
    private MicroserviceSystem microserviceSystem;

    @Before
    public void setUp(){
        FileUtils.createPaths();

        IRExtractionService irExtractionService = new IRExtractionService("./test_config.json");

        irExtractionService.generateIR("TestIR.json");

        microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);
        
        wrongCutsService = new WrongCutsService();
    }

    @Test
    public void testWrongCutsDetection(){
        WrongCuts wrongCuts = wrongCutsService.detectWrongCuts(microserviceSystem);

        assertTrue(wrongCuts.numWrongCuts() > 0);

        List<String> expectedWrongCuts = new ArrayList<>(Arrays.asList("microservice-a", "microservice-c", "microservice-d"));
        Collections.sort(wrongCuts.getWrongCuts());

        assertTrue(wrongCuts.getWrongCuts().equals(expectedWrongCuts));
    }
}
