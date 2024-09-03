package edu.university.ecs.lab.detection.antipatterns;

import com.google.gson.Gson;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.MethodDependencyGraph;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.detection.antipatterns.models.*;
import edu.university.ecs.lab.detection.antipatterns.services.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

/**
 * Class to detect multiple antipatterns from IR of a given system
 */
public class AntipatternDetection {
    public static void main(String[] args) {
        // Create IR of first commit
        createIRSystem("./configs/config_spring-boot-microservices.json", "IR.json");

        // Creat Microservice System based on generated IR
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);

        // Create service network graph and method network graph from Microservice System, write to JSON
        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);
        MethodDependencyGraph mdg = new MethodDependencyGraph(currentSystem);

        writeObjectToJsonFile(sdg.toJsonObject(), "sdg.json");
        writeObjectToJsonFile(mdg.toJsonObject(), "mdg.json");

        int detectedAntipatterns = 0;

        // Detect greedy microservices based on a default REST call threshold of 6
        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        if (!greedyMicroservices.getGreedyMicroservices().isEmpty()){
            System.out.println("Greedy Services detected");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/greedy.json", greedyMicroservices.toJsonObject());
        }
        
        // Detect hublike microservices based on a default REST call threshold of 6
        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        if (!hublikeMicroservices.getHublikeMicroservices().isEmpty()){
            System.out.println("Hublike Services detected");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/hublike.json", hublikeMicroservices.toJsonObject());
        }
        
        // Detect microservice chains based on a default chain length of 3
        ServiceChainMSLevelService chainService = new ServiceChainMSLevelService();
        ServiceChain allChains = chainService.getServiceChains(sdg);
        if (!allChains.getChain().isEmpty()){
            System.out.println("Service chains detected (Service level)");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/servicechainMSlevel.json", allChains.toJsonObject());
        }

        // Detect method chains based on a default chain length of 3
        ServiceChainMethodLevelService chainService2 = new ServiceChainMethodLevelService();
        ServiceChain allChains2 = chainService2.getServiceChains(mdg);
        if (!allChains2.getChain().isEmpty()){
            System.out.println("Service chains detected (Method level)");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/servicechainMethodlevel.json", allChains2.toJsonObject());
        }

        // Detect clusters of wrongly interconnected services
        WrongCutsService wrongCutsService = new WrongCutsService();
        WrongCuts wrongCuts = wrongCutsService.detectWrongCuts(currentSystem);
        if (!wrongCuts.getWrongCuts().isEmpty()){
            System.out.println("Wrongs cuts detected");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/wrongcuts.json", wrongCuts.toJsonObject());
        }
        
        // Detect cyclic microservice dependencies
        CyclicDependencyMSLevelService cycles = new CyclicDependencyMSLevelService();
        CyclicDependency cycleDependencies = cycles.findCyclicDependencies(sdg);
        if (!cycleDependencies.getCycles().isEmpty()){
            System.out.println("Cyclic dependencies detected (Service level)");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/cyclicdependenciesMSlevel.json", cycleDependencies.toJsonObject());
        }

        // Detect cyclic method dependencies
        CyclicDependencyMethodLevelService cycles2 = new CyclicDependencyMethodLevelService();
        CyclicDependency cycleDependencies2 = cycles2.findCyclicDependencies(mdg);
        if (!cycleDependencies2.getCycles().isEmpty()){
            System.out.println("Cyclic dependencies detected (Method level)");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/cyclicdependenciesMethodlevel.json", cycleDependencies2.toJsonObject());
        }

        // Check presence of health check configurations
        NoHealthcheckService noHealthCheckService = new NoHealthcheckService();
        NoHealthcheck noHealthCheck = noHealthCheckService.checkHealthcheck(currentSystem);
        if (!noHealthCheck.getnoHealthcheck().isEmpty()){
            System.out.println("No heath check detected");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/nohealthcheck.json", noHealthCheck.toJsonObject());
        }

        // Detect wobbly microservice interactions
        WobblyServiceInteractionService wobbly = new WobblyServiceInteractionService();
        WobblyServiceInteraction wobblyService = wobbly.findWobblyServiceInteractions(currentSystem);
        if (!wobblyService.getWobblyServiceInteractions().isEmpty()){
            System.out.println("Wobbly service interactions detected");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/wobblyserviceinteratcions.json", wobblyService.toJsonObject());
        }

        // Detect the presence of API gateway configuration
        NoApiGatewayService noApiGatewayService = new NoApiGatewayService();
        NoApiGateway noApiGateway = noApiGatewayService.checkforApiGateway(currentSystem);
        if (noApiGateway.getnoApiGateway()){
            System.out.println("No API Gateway detected");
            detectedAntipatterns++;
            JsonReadWriteUtils.writeToJSON("./output/noapigateway.json", noApiGateway.toJsonObject());
        }

        System.out.println("Number of Anti-Patterns Detected: " + detectedAntipatterns);

    }

    /**
     * Method to create an IR from config file
     * 
     * @param configPath path to configuration file
     * @param fileName name of output file for IR extraction
     */
    private static void createIRSystem(String configPath, String fileName) {
        // Create both directories needed
        FileUtils.makeDirs();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(configPath, Optional.empty());

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    /**
     * Method to write dependency graph objects to JSON
     * 
     * @param <T> generalized json class
     * @param object json object to be written to file
     * @param filename name of output JSON file
     */
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
