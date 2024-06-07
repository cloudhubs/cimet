package edu.university.ecs.lab.temporal;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemporalRunner {

    public static void main(String[] args) {

        Config config = ConfigUtil.readConfig("./config.json");
        ConfigUtil.createPaths();
        GitService gitService = new GitService(config);

        Iterable<RevCommit> commits = gitService.getLog();

        Iterator<RevCommit> iterator = commits.iterator();
        List<RevCommit> list = new LinkedList<>();
        while(iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.reverse(list);

//        for(int i = 0; i < list.size() - 1; i++) {
//            String commitId = list.get(i).toString().split(" ")[1];
//            config.setBaseCommit(commitId);
//            gitService.resetLocal(commitId);
//            createIRSystem(config);
//            computeGraph("./output/rest-extraction-output-[main-" + commitId.substring(0,7) + "].json", commitId.substring(0,7));
//        }

        String commitId = "3dd8eb99b8ec16b3b93d7709bff56a45e637bfc7";
        config.setBaseCommit(commitId);
        gitService.resetLocal(commitId);
        createIRSystem(config);
        computeGraph("./output/rest-extraction-output-[main-" + commitId.substring(0,7) + "].json", commitId.substring(0,7));

    }


    private static void createIRSystem(Config config) {
        // Create both directories needed
        ConfigUtil.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(config);

        // Generate the Intermediate Representation
        irExtractionService.generateIR();
    }

    private static void computeGraph(String filePath, String commitID) {
        MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON(filePath, MicroserviceSystem.class);

        List<MethodCall> restCalls = new ArrayList<>();
        for(Microservice microservice : microserviceSystem.getMicroservices()) {
            restCalls.addAll(microservice.getServices().stream().flatMap(jClass -> jClass.getMethodCalls().stream()).filter(methodCall -> methodCall instanceof RestCall).collect(Collectors.toUnmodifiableList()));
        }

        List<Method> endpoints = new ArrayList<>();
        for(Microservice microservice : microserviceSystem.getMicroservices()) {
            endpoints.addAll(microservice.getControllers().stream().flatMap(jClass -> jClass.getMethods().stream()).filter(method -> method instanceof Endpoint).collect(Collectors.toUnmodifiableList()));
        }

        Set<String> nodes = new HashSet<>();
        List<Edge> edges = new ArrayList<>();
        for(MethodCall methodCall : restCalls) {
            for(Method method : endpoints) {
                RestCall restCall = (RestCall) methodCall;
                Endpoint endpoint = (Endpoint) method;
                if(restCall.getUrl().equals(endpoint.getUrl()) && restCall.getHttpMethod().equals(endpoint.getHttpMethod())
                    && !restCall.getMicroserviceName().equals(endpoint.getMicroserviceName())) {
                    edges.add(new Edge(restCall.getMicroserviceName(), endpoint.getMicroserviceName(), endpoint.getUrl(), 0));
                    nodes.add(endpoint.getMicroserviceName());
                    nodes.add(restCall.getMicroserviceName());
                }
            }
        }

        Set<Edge> edgeSet = new HashSet<>();
        Map<Edge, Long> edgeDuplicateMap = edges.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        edgeSet = edgeDuplicateMap.entrySet().stream().map(entry -> {
            Edge edge = entry.getKey();
            edge.setWeight(Math.toIntExact(entry.getValue()));
            return edge;
        }).collect(Collectors.toSet());

        NetworkGraph networkGraph = new NetworkGraph("Graph", commitID, true, false, nodes, edgeSet);
        JsonReadWriteUtils.writeToJSON("./output/" + commitID + "-graph.json", networkGraph);
    }



}
