package antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.NoApiGateway;
import edu.university.ecs.lab.detection.antipatterns.services.NoApiGatewayService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

public class NoApiGatewayTest {
    private NoApiGatewayService noApiGatewayService;
    private MicroserviceSystem microserviceSystem;

    @Before
    public void setUp(){
        FileUtils.createPaths();

        IRExtractionService irExtractionService = new IRExtractionService("./test_config.json");

        irExtractionService.generateIR("TestIR.json");

        microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);

        noApiGatewayService = new NoApiGatewayService();
    }

    @Test
    public void testNoAPIGatewayDetection(){
        NoApiGateway noApiGateway = noApiGatewayService.checkforApiGateway(microserviceSystem);

        boolean expectedNoApiGateway = false;

        assertTrue(noApiGateway.getnoApiGateway() == expectedNoApiGateway);
    }

}
