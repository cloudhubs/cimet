
package unit.antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.detection.antipatterns.models.ServiceChain;
import edu.university.ecs.lab.detection.antipatterns.services.CyclicDependencyMSLevelService;
import edu.university.ecs.lab.detection.antipatterns.services.ServiceChainMSLevelService;
import org.junit.Before;
import org.junit.Test;

import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.antipatterns.models.CyclicDependency;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import unit.Constants;

public class CyclicDependencyTest {
    private CyclicDependencyMSLevelService cyclicService;
    private ServiceDependencyGraph sdg;

    @Before
    public void setUp(){
//        FileUtils.makeDirs();
//
//        IRExtractionService irExtractionService = new IRExtractionService(Constants.TEST_CONFIG_PATH, Optional.empty());
//
//        irExtractionService.generateIR("TestIR.json");
//
//        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/TestIR.json", MicroserviceSystem.class);
//
//        sdg = new ServiceDependencyGraph(microserviceSystem);
//
//        cyclicService = new CyclicDependencyMSLevelService();
    }

    public void testCyclicDependencyDetection(){
        CyclicDependency cyclicDep = cyclicService.findCyclicDependencies(sdg);

        assertTrue(cyclicDep.numCyclicDep() > 0);

        List<List<String>> expectedCyclicDep = List.of(
                Arrays.asList("microservice-b", "microservice-d", "microservice-b"),
                Arrays.asList("microservice-a", "microservice-c", "microservice-a"));

        assertTrue(Objects.equals(cyclicDep.getCycles(), expectedCyclicDep));
    }

    @Test
    public void cyclicDependencyHasNOne() {
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


        MicroserviceSystem microserviceSystem1 = new MicroserviceSystem("test", "1", Set.of(microservice1, microservice2, microservice3), new HashSet<>());
        ServiceDependencyGraph sdg1 = new ServiceDependencyGraph(microserviceSystem1);
        CyclicDependencyMSLevelService cs1 = new CyclicDependencyMSLevelService();

        CyclicDependency cd = cs1.findCyclicDependencies(sdg1);
        assertTrue(cd.getCycles().size() == 0);

    }

    @Test
    public void cyclicDependencyHasOne() {
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
        CyclicDependencyMSLevelService cs1 = new CyclicDependencyMSLevelService();

        CyclicDependency cd = cs1.findCyclicDependencies(sdg1);
        assertTrue(cd.getCycles().size() == 1);
        assertTrue(cd.getCycles().get(0).size() == 3);
        assertTrue(cd.getCycles().get(0).containsAll(List.of("ms1", "ms2", "ms3")));

    }

    @Test
    public void cyclicDependencyHasOneAlso() {
        Microservice microservice1 = new Microservice("ms1", "/ms1");
        JClass jClass1 = new JClass("class1", "/class1","class1", ClassRole.CONTROLLER);
        jClass1.setMethods(Set.of(new Endpoint(new Method(), "/endpoint1", HttpMethod.GET)));
        microservice1.addJClass(jClass1);
        JClass jClass2 = new JClass("class5", "/class2","class5", ClassRole.SERVICE);
        jClass2.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint2", HttpMethod.GET)));
        microservice1.addJClass(jClass2);

        Microservice microservice2 = new Microservice("ms2", "/ms2");
        JClass jClass3 = new JClass("class2", "/class3","class2", ClassRole.SERVICE);
        jClass3.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint1", HttpMethod.GET)));
        microservice2.addJClass(jClass3);
        JClass jClass4 = new JClass("class3", "/class4","class3", ClassRole.CONTROLLER);
        jClass4.setMethods(Set.of(new Endpoint(new Method(), "/endpoint2", HttpMethod.GET)));
        microservice2.addJClass(jClass4);




        MicroserviceSystem microserviceSystem1 = new MicroserviceSystem("test", "1", Set.of(microservice1, microservice2), new HashSet<>());
        ServiceDependencyGraph sdg1 = new ServiceDependencyGraph(microserviceSystem1);
        CyclicDependencyMSLevelService cs1 = new CyclicDependencyMSLevelService();

        CyclicDependency cd = cs1.findCyclicDependencies(sdg1);
        assertTrue(cd.getCycles().size() == 1);
        assertTrue(cd.getCycles().get(0).size() == 2);
        assertTrue(cd.getCycles().get(0).containsAll(List.of("ms1", "ms2")));

    }

    @Test
    public void cyclicDependencyHasTwo() {
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
        JClass jClass9 = new JClass("class9", "/class9","class9", ClassRole.SERVICE);
        jClass9.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint4", HttpMethod.GET)));
        microservice3.addJClass(jClass9);
        JClass jClass6 = new JClass("class6", "/class6","class6", ClassRole.CONTROLLER);
        jClass6.setMethods(Set.of(new Endpoint(new Method(), "/endpoint3", HttpMethod.GET)));
        microservice3.addJClass(jClass6);

        Microservice microservice4 = new Microservice("ms4", "/ms4");
        JClass jClass7 = new JClass("jClass7", "/jClass7","jClass7", ClassRole.SERVICE);
        jClass7.setMethodCalls(Set.of(new RestCall(new MethodCall(), "/endpoint3", HttpMethod.GET)));
        microservice4.addJClass(jClass7);
        JClass jClass8 = new JClass("jClass8", "/jClass8","jClass8", ClassRole.CONTROLLER);
        jClass8.setMethods(Set.of(new Endpoint(new Method(), "/endpoint4", HttpMethod.GET)));
        microservice4.addJClass(jClass8);


        MicroserviceSystem microserviceSystem1 = new MicroserviceSystem("test", "1", Set.of(microservice1, microservice2, microservice3, microservice4), new HashSet<>());
        ServiceDependencyGraph sdg1 = new ServiceDependencyGraph(microserviceSystem1);
        CyclicDependencyMSLevelService cs1 = new CyclicDependencyMSLevelService();

        CyclicDependency cd = cs1.findCyclicDependencies(sdg1);
        assertTrue(cd.getCycles().size() == 2);
        assertTrue(cd.getCycles().get(0).size() == 3);
        assertTrue(cd.getCycles().get(1).size() == 2);
        assertTrue(cd.getCycles().get(0).containsAll(List.of("ms1", "ms2", "ms3")));
        assertTrue(cd.getCycles().get(1).containsAll(List.of("ms3", "ms4")));

    }
}

