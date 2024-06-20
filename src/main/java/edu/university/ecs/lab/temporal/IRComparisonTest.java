package edu.university.ecs.lab.temporal;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.*;
import edu.university.ecs.lab.common.models.sdg.EndpointCallEdge;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IRComparisonTest {

    public static void main(String[] args) {

        Config config = ConfigUtil.readConfig("./config.json");
        DeltaExtractionService deltaExtractionService;
        FileUtils.createPaths();
        GitService gitService = new GitService(config);

        Iterable<RevCommit> commits = gitService.getLog();

        Iterator<RevCommit> iterator = commits.iterator();
        List<RevCommit> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.reverse(list);
        config.setBaseCommit(list.get(1).toString().split(" ")[1]);
        // Create IR of first commit
        createIRSystem(config, "IR.json");


        // Loop through commit history and create delta, merge, etc...
        for (int i = 0; i < list.size() - 1; i++) {
            String commitIdOld = list.get(i).toString().split(" ")[1];
            String commitIdNew = list.get(i + 1).toString().split(" ")[1];

            // Extract changes from one commit to the other
            deltaExtractionService = new DeltaExtractionService("./config.json", commitIdOld, commitIdNew);
            deltaExtractionService.generateDelta();

            // Merge Delta changes to old IR to create new IR representing new commit changes
            MergeService mergeService = new MergeService("./output/IR.json", "./output/Delta.json", "./config.json");
            mergeService.generateMergeIR();
            //computeGraph("./output/rest-extraction-output-[main-" + commitIdNew.substring(0,7) + "].json", commitIdNew.substring(0,7));
        }

        // Create IR of last commit
        config.setBaseCommit(list.get(list.size() - 1).toString().split(" ")[1]);
        createIRSystem(config, "IRCompare.json");

        // Compare two IR's for equivalence
        MicroserviceSystem microserviceSystem1 = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);
        microserviceSystem1.setCommitID(config.getBaseCommit());
        MicroserviceSystem microserviceSystem2 = JsonReadWriteUtils.readFromJSON("./output/IRCompare.json", MicroserviceSystem.class);
        boolean b = Objects.deepEquals(microserviceSystem1, microserviceSystem2);


        // Output results
        System.out.println(b);

    }


    private static void createIRSystem(Config config, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(config);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    @Deprecated
    private static void computeGraph(String filePath, String commitID) {
        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON(filePath, MicroserviceSystem.class);

        List<MethodCall> restCalls = new ArrayList<>();
        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            restCalls.addAll(microservice.getServices().stream().flatMap(jClass -> jClass.getMethodCalls().stream()).filter(methodCall -> methodCall instanceof RestCall).collect(Collectors.toUnmodifiableList()));
        }

        List<Method> endpoints = new ArrayList<>();
        for (Microservice microservice : microserviceSystem.getMicroservices()) {
            endpoints.addAll(microservice.getControllers().stream().flatMap(jClass -> jClass.getMethods().stream()).filter(method -> method instanceof Endpoint).collect(Collectors.toUnmodifiableList()));
        }

        Set<String> nodes = new HashSet<>();
        List<EndpointCallEdge> edges = new ArrayList<>();
        for (MethodCall methodCall : restCalls) {
            for (Method method : endpoints) {
                RestCall restCall = (RestCall) methodCall;
                Endpoint endpoint = (Endpoint) method;
                if (restCall.getUrl().equals(endpoint.getUrl()) && restCall.getHttpMethod().equals(endpoint.getHttpMethod())
                        && !restCall.getMicroserviceName().equals(endpoint.getMicroserviceName())) {
                    edges.add(new EndpointCallEdge(restCall.getMicroserviceName(), endpoint.getMicroserviceName(), endpoint.getUrl(), 0));
                    nodes.add(endpoint.getMicroserviceName());
                    nodes.add(restCall.getMicroserviceName());
                }
            }
        }

        Set<EndpointCallEdge> edgeSet = new HashSet<>();
        Map<EndpointCallEdge, Long> edgeDuplicateMap = edges.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        edgeSet = edgeDuplicateMap.entrySet().stream().map(entry -> {
            EndpointCallEdge edge = entry.getKey();
            edge.setWeight(Math.toIntExact(entry.getValue()));
            return edge;
        }).collect(Collectors.toSet());

        ServiceDependencyGraph serviceDependencyGraph = new ServiceDependencyGraph("Graph", commitID, nodes, edgeSet);
        JsonReadWriteUtils.writeToJSON("./output/" + commitID + "-graph.json", serviceDependencyGraph);
    }


}
