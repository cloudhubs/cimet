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

public class AntipatternDetection {
    public static void main(String[] args) {

        Config config = ConfigUtil.readConfig("./test_config.json");

        // Create IR of first commit
        createIRSystem(config, "IR.json");

        // Creat Microservice System based on generated IR
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);

        writeObjectToJsonFile(sdg.toJsonObject(), "sdg.json");

        int detectedAntipatterns = 0;

        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        if (!greedyMicroservices.getGreedyMicroservices().isEmpty()){
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/greedy.json", greedyMicroservices.toJsonObject());
        }
        
        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        if (!hublikeMicroservices.getHublikeMicroservices().isEmpty()){
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/hublike.json", hublikeMicroservices.toJsonObject());
        }
        

        ServiceChainService chainService = new ServiceChainService();
        ServiceChain allChains = chainService.getServiceChains(sdg);
        if (!allChains.getChain().isEmpty()){
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/servicechain.json", allChains.toJsonObject());
        }
        
        WrongCutsService wrongCutsService = new WrongCutsService();
        WrongCuts wrongCuts = wrongCutsService.detectWrongCuts(currentSystem);
        if (!wrongCuts.getWrongCuts().isEmpty()){
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/wrongcuts.json", wrongCuts.toJsonObject());
        }
        
        CyclicDependencyService cycles = new CyclicDependencyService();
        CyclicDependency cycleDependencies = cycles.findCyclicDependencies(sdg);
        if (!cycleDependencies.getCycles().isEmpty()){
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/cyclicdependencies.json", cycleDependencies.toJsonObject());
        }
        
        NoHealthcheckService noHealthCheckService = new NoHealthcheckService();
        NoHealthcheck noHealthCheck = noHealthCheckService.checkHealthcheck("./healthcheck.yaml");
        if (noHealthCheck.getnoHealthcheck()){
            detectedAntipatterns++;
        }

        WobblyServiceInteractionService wobbly = new WobblyServiceInteractionService();
        WobblyServiceInteraction wobblyService = wobbly.findWobblyServiceInteractions(currentSystem);
        if (!wobblyService.getWobblyServiceInteractions().isEmpty()){
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/wobblyserviceinteratcions.json", wobblyService.toJsonObject());
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
