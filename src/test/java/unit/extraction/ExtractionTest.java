package unit.extraction;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtractionTest {
    private static final String TEST_FILE1 = "src/test/resources/TestFile2.java";
    private static final String TEST_FILE2 = "src/test/resources/TestFile3.java";


    private static final String TEST_CONFIG_FILE = "src/test/resources/test_config.json";
    private static final int EXPECTED_CALLS = 6;
    private static final String PRE_URL = "/api/v1/seatservice/test";

    @Before
    public void setUp() {
    }

    @Test
    public void restCallExtractionTest1() {
        GitService gitService = new GitService(TEST_CONFIG_FILE);
        MicroserviceSystem ms1 = JsonReadWriteUtils.readFromJSON("C:\\Users\\ninja\\IdeaProjects\\cimet2\\output\\java-microservice\\IR\\IR1_8948.json", MicroserviceSystem.class);
        JClass jClass1 = SourceToObjectUtils.parseClass(new File(TEST_FILE1), ConfigUtil.readConfig(TEST_CONFIG_FILE), "");
        JClass jClass2 = SourceToObjectUtils.parseClass(new File(TEST_FILE2), ConfigUtil.readConfig(TEST_CONFIG_FILE), "");

        for(Endpoint e : jClass1.getEndpoints()) {
             for(RestCall rc : jClass2.getRestCalls()) {
                 if(RestCall.matchEndpoint(rc, e)) {
                     System.out.println("Passed " + rc.getUrl() + " " + e.getUrl());

                 }
             }

        }

//
//        int i = 1;
//        for(RestCall restCall : jClass.getRestCalls()) {
//            assertTrue(restCall.getUrl().startsWith(PRE_URL + i++));
//        }

    }


}
