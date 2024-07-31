//package antipatterns;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.*;
//
//import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
//import edu.university.ecs.lab.common.utils.FileUtils;
//import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
//import edu.university.ecs.lab.detection.antipatterns.models.NoHealthcheck;
//import edu.university.ecs.lab.detection.antipatterns.services.NoHealthcheckService;
//import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
//
//public class NoHealthcheckTest {
//    private NoHealthcheckService noHealthcheckService;
//    private MicroserviceSystem microserviceSystem;
//
//    @Before
//    public void setUp(){
//        FileUtils.makeDirs();
//
//        IRExtractionService irExtractionService = new IRExtractionService("./test_config.json");
//
//        irExtractionService.generateIR("TestIR.json");
//
//        microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);
//
//        noHealthcheckService = new NoHealthcheckService();
//    }
//
//    @Test
//    public void testNoHealthCheck(){
//        NoHealthcheck noHealthcheck = noHealthcheckService.checkHealthcheck(microserviceSystem);
//
//        Map<String, Boolean> expectedNoHealthcheck = new HashMap<>();
//
//        expectedNoHealthcheck.put("microservice-d", false);
//        expectedNoHealthcheck.put("microservice-a", true);
//        expectedNoHealthcheck.put("microservice-b", false);
//        expectedNoHealthcheck.put("microservice-c", false);
//        expectedNoHealthcheck.put("gateway", false);
//
//        assertTrue(Objects.equals(noHealthcheck.getnoHealthcheck(), expectedNoHealthcheck));
//    }
//}
