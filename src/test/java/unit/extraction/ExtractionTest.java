package unit.extraction;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.RestCall;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtractionTest {
    private static final String TEST_FILE = "src/test/resources/TestFile.java";
    private static final String TEST_CONFIG_FILE = "src/test/resources/test_config.json";
    private static final int EXPECTED_CALLS = 5;
    private static final String PRE_URL = "/api/v1/seatservice/test";

    @Before
    public void setUp() {
    }

    @Test
    public void restCallExtractionTest1() {
        JClass jClass = SourceToObjectUtils.parseClass(new File(TEST_FILE), ConfigUtil.readConfig(TEST_CONFIG_FILE), "");

        assertEquals(EXPECTED_CALLS, jClass.getRestCalls().size());

        int i = 1;
        for(RestCall restCall : jClass.getRestCalls()) {
            assertTrue(restCall.getUrl().startsWith(PRE_URL + i++));
        }

    }


}
