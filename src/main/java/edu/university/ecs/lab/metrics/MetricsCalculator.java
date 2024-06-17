package edu.university.ecs.lab.metrics;

import com.google.gson.Gson;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.common.models.NetworkGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.metrics.models.metrics.*;
import edu.university.ecs.lab.metrics.services.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MetricsCalculator {
    public static void main(String[] args) {

        Config config = ConfigUtil.readConfig("./config.json");

        // Create IR of first commit
        createIRSystem(config, "IR.json");

        // Creat Microservice System based on generated IR
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);

        NetworkGraph sdg = new NetworkGraph();
        sdg.createGraph(currentSystem);

        writeObjectToJsonFile(sdg, "networkgraph.json");

        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        writeObjectToJsonFile(greedyMicroservices, "greedy.json");

        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        writeObjectToJsonFile(hublikeMicroservices, "hublike.json");

        ServiceChainService chainService = new ServiceChainService();
        List<ServiceChain> allChains = chainService.getServiceChains(sdg);
        writeObjectToJsonFile(allChains, "servicechain.json");

        WrongCutsService wrongCutsService = new WrongCutsService();
        List<WrongCuts> wrongCuts = wrongCutsService.identifyAndReportWrongCuts(sdg);
        writeObjectToJsonFile(wrongCuts, "wrongcuts.json");

        CyclicDependencyService cycles = new CyclicDependencyService();
        List<CyclicDependency> cycleDepencies = cycles.findCyclicDependencies(sdg);
        writeObjectToJsonFile(cycleDepencies, "cyclicdependencies.json");
    }

    private static void createIRSystem(Config config, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(config);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    public static <T> void writeObjectToJsonFile(T object, String filename) {
        Gson gson = new Gson();
        String json = gson.toJson(object);

        try (FileWriter fileWriter = new FileWriter("./output/" + filename)) {
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Successfully wrote rest extraction to: \"" + filename + "\"");
    }
}
