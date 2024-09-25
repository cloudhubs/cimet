package unit.antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.detection.antipatterns.services.ServiceChainMSLevelService;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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

    @Test
    public void serviceChainHasOne() {
        Microservice microservice1 = new Microservice("ms1", "/ms1");
        JClass jClass1 = new JClass("class1", "/class1","class1", ClassRole.CONTROLLER);
        jClass1.setMethods(Set.of(new Endpoint(new Method(), "/endpoint1", HttpMethod.GET)));
        microservice1.addJClass(jClass1);

        Microservice microservice2 = new Microservice("ms2", "/ms2");
        JClass jClass2 = new JClass("class2", "/class2","class2", ClassRole.SERVICE);
        jClass2.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint1", HttpMethod.GET)));
        microservice2.addJClass(jClass2);
        JClass jClass3 = new JClass("class3", "/class3","class3", ClassRole.CONTROLLER);
        jClass3.setMethods(Set.of(new Endpoint(new Method(), "/endpoint2", HttpMethod.GET)));
        microservice2.addJClass(jClass3);

        Microservice microservice3 = new Microservice("ms3", "/ms3");
        JClass jClass4 = new JClass("class4", "/class4","class4", ClassRole.SERVICE);
        jClass4.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint2", HttpMethod.GET)));
        microservice3.addJClass(jClass4);

        MicroserviceSystem microserviceSystem1 = new MicroserviceSystem("test", "1", Set.of(microservice1, microservice2, microservice3), new HashSet<>());
        ServiceDependencyGraph sdg1 = new ServiceDependencyGraph(microserviceSystem1);
        ServiceChainMSLevelService scs1 = new ServiceChainMSLevelService();

        ServiceChain sc1 = scs1.getServiceChains(sdg1);

        assertTrue(sc1.getChain().size() == 1);
        assertEquals(sc1.getChain().get(0), List.of("ms3", "ms2", "ms1"));
    }

    @Test
    public void serviceChainHasNoneCycle() {
        Microservice microservice1 = new Microservice("ms1", "/ms1");
        JClass jClass1 = new JClass("class1", "/class1","class1", ClassRole.CONTROLLER);
        jClass1.setMethods(Set.of(new Endpoint(new Method(), "/endpoint1", HttpMethod.GET)));
        microservice1.addJClass(jClass1);
        JClass jClass5 = new JClass("class5", "/class5","class5", ClassRole.SERVICE);
        jClass5.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint3", HttpMethod.GET)));
        microservice1.addJClass(jClass5);

        Microservice microservice2 = new Microservice("ms2", "/ms2");
        JClass jClass2 = new JClass("class2", "/class2","class2", ClassRole.SERVICE);
        jClass2.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint1", HttpMethod.GET)));
        microservice2.addJClass(jClass2);
        JClass jClass3 = new JClass("class3", "/class3","class3", ClassRole.CONTROLLER);
        jClass3.setMethods(Set.of(new Endpoint(new Method(), "/endpoint2", HttpMethod.GET)));
        microservice2.addJClass(jClass3);

        Microservice microservice3 = new Microservice("ms3", "/ms3");
        JClass jClass4 = new JClass("class4", "/class4","class4", ClassRole.SERVICE);
        jClass4.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint2", HttpMethod.GET)));
        microservice3.addJClass(jClass4);
        JClass jClass6 = new JClass("class6", "/class6","class6", ClassRole.CONTROLLER);
        jClass6.setMethods(Set.of(new Endpoint(new Method(), "/endpoint3", HttpMethod.GET)));
        microservice3.addJClass(jClass6);


        MicroserviceSystem microserviceSystem1 = new MicroserviceSystem("test", "1", Set.of(microservice1, microservice2, microservice3), new HashSet<>());
        ServiceDependencyGraph sdg1 = new ServiceDependencyGraph(microserviceSystem1);
        ServiceChainMSLevelService scs1 = new ServiceChainMSLevelService();

        ServiceChain sc1 = scs1.getServiceChains(sdg1);
        assertTrue(sc1.getChain().isEmpty());

    }
}
