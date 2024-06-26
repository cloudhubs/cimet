package edu.university.ecs.lab.detection.antipatterns;

import com.google.gson.Gson;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.detection.antipatterns.models.*;
import edu.university.ecs.lab.detection.antipatterns.services.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class AntipatternDetection {
    public static void main(String[] args) {

        Config config = ConfigUtil.readConfig("./config.json");

        // Create IR of first commit
        createIRSystem(config, "IR.json");

        // Creat Microservice System based on generated IR
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);

        writeObjectToJsonFile(sdg.toJsonObject(), "sdg.json");

        int detectedAntipatterns = 0;

        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        if (!greedyMicroservices.isEmpty()){
            detectedAntipatterns++;
            writeObjectToJsonFile(greedyMicroservices, "greedy.json");
        }
        
        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        if (!hublikeMicroservices.isEmpty()){
            detectedAntipatterns++;
            writeObjectToJsonFile(hublikeMicroservices, "hublike.json");
        }
        

        ServiceChainService chainService = new ServiceChainService();
        List<ServiceChain> allChains = chainService.getServiceChains(sdg);
        if (!allChains.isEmpty()){
            detectedAntipatterns++;
            writeObjectToJsonFile(allChains, "servicechain.json");
        }
        
        WrongCutsService wrongCutsService = new WrongCutsService();
        List<WrongCuts> wrongCuts = wrongCutsService.identifyAndReportWrongCuts(sdg);
        if (!wrongCuts.isEmpty()){
            detectedAntipatterns++;
            writeObjectToJsonFile(wrongCuts, "wrongcuts.json");
        }
        
        CyclicDependencyService cycles = new CyclicDependencyService();
        List<CyclicDependency> cycleDependencies = cycles.findCyclicDependencies(sdg);
        if (!cycleDependencies.isEmpty()){
            detectedAntipatterns++;
            writeObjectToJsonFile(cycleDependencies, "cyclicdependencies.json");
        }
        
        NoHealthcheckService noHealthCheckService = new NoHealthcheckService();
        NoHealthcheck noHealthCheck = noHealthCheckService.checkHealthcheck("./healthcheck.yaml");
        if (noHealthCheck.getnoHealthcheck()){
            detectedAntipatterns++;
        }

        WobblyServiceInteractionService wobbly = new WobblyServiceInteractionService();
        List<WobblyServiceInteraction> wobblyService = wobbly.checkForWobblyServiceInteractions(currentSystem);
        if (!wobblyService.isEmpty()){
            detectedAntipatterns++;
            writeObjectToJsonFile(wobblyService, "wobblyserviceinteractions.json");
        }

        NoApiGatewayService noApiGatewayService = new NoApiGatewayService();
        NoApiGateway noApiGateway = noApiGatewayService.checkforApiGateway("./apigateway.yaml");
        if (noApiGateway.getnoApiGateway()){
            detectedAntipatterns++;
        }

        System.out.println("Number of Anti-Patterns Detected: " + detectedAntipatterns);

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
