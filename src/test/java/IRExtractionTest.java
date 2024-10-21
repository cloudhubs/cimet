import org.junit.jupiter.api.Test;

import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

public class IRExtractionTest {

    public static final String TEST_CONFIG_PATH = "/resources/test_config.json";
    
    @Test
    void testCreate() {
        MicroserviceSystem newSystem = IRExtractionService.create(TEST_CONFIG_PATH);
    }
}
