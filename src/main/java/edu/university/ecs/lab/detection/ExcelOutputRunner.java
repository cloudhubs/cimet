package edu.university.ecs.lab.detection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.jgit.revwalk.RevCommit;

import com.google.gson.JsonArray;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.*;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.detection.antipatterns.models.*;
import edu.university.ecs.lab.detection.antipatterns.services.*;
import edu.university.ecs.lab.detection.metrics.RunCohesionMetrics;
import edu.university.ecs.lab.detection.metrics.models.*;
import edu.university.ecs.lab.detection.metrics.services.*;
import edu.university.ecs.lab.detection.architecture.models.*;
import edu.university.ecs.lab.detection.architecture.services.*;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;

import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelOutputRunner {

    private static final String[] columnLabels = new String[]{"Commit ID", "Greedy Microservices", "Hub-like Microservices", "Service Chains (MS level)", "Service Chains (method level)",
            "Wrong Cuts", "Cyclic Dependencies (MS level)", "Cyclic Dependencies (Method level)", "Wobbly Service Interactions",  "No Healthchecks",
            "No API Gateway", "maxAIS", "avgAIS", "stdAIS", "maxADC", "ADCS", "stdADS", "maxACS", "avgACS", "stdACS", "SCF", "SIY", "maxSC", "avgSC",
            "stdSC", "SCCmodularity", "maxSIDC", "avgSIDC", "stdSIDC", "maxSSIC", "avgSSIC", "stdSSIC",
            "maxLOMLC", "avgLOMLC", "stdLOMLC", "AR3 (System)","AR4 (System)", "AR6 (Delta)", "AR20 (System)"};

    private static final int ANTIPATTERNS = 10;
    private static final int METRICS = 24;
    private static final int ARCHRULES = 4;
    private static final String OLD_IR_PATH = "./output/OldIR.json";
    private static final String DELTA_PATH = "./output/Delta.json";
    private static final String NEW_IR_PATH = "./output/NewIR.json";

    public static void main(String[] args) throws IOException {

        if (columnLabels.length != ANTIPATTERNS+ METRICS+ARCHRULES+1) { // Sanity check
            throw new RuntimeException("ExcelOutputRunner misconfigured - amount of columns does not add up");
        }
        String config_path = args[0];
        Config config = ConfigUtil.readConfig(config_path);
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
        config.setBaseCommit(list.get(0).toString().split(" ")[1]);
        // Create IR of first commit
        createIRSystem(config_path, "OldIR.json");

        //Create excel file and desired header labels
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet(config.getSystemName());

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnLabels.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnLabels[i]);
        }

        // Used for AR validation
        List<List<AbstractAR>> allARs = new ArrayList<>();

        for (int i = 1; i < list.size() - 1; i++) {
            String commitIdOld = list.get(i).toString().split(" ")[1];
            String commitIdNew = list.get(i + 1).toString().split(" ")[1];

            Map<String, Integer> allAntiPatterns = new HashMap<>();
            HashMap<String, Double> metrics = new HashMap<>();
            List<AbstractAR> currARs = new ArrayList<>();

            MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON(OLD_IR_PATH, MicroserviceSystem.class);

            // Extract changes from one commit to the other
            deltaExtractionService = new DeltaExtractionService(config_path, OLD_IR_PATH, commitIdOld, commitIdNew);
            deltaExtractionService.generateDelta();

            // Merge Delta changes to old IR to create new IR representing new commit changes
            MergeService mergeService = new MergeService(OLD_IR_PATH, DELTA_PATH, config_path);
            mergeService.generateMergeIR();

            if (!microserviceSystem.getMicroservices().isEmpty()) {
                detectAntipatterns(microserviceSystem,allAntiPatterns, metrics, currARs);
            }

            updateExcel(sheet, commitIdOld, allAntiPatterns, metrics, currARs, i);

            try {
                Files.move(Paths.get(NEW_IR_PATH), Paths.get(OLD_IR_PATH), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            allARs.add(currARs);

        }

        try (FileOutputStream fileOut = new FileOutputStream(String.format("./output/AntiPatterns_%s.xlsx", config.getSystemName()))) {
            workbook.write(fileOut);
            System.out.printf("Excel file created: AntiPatterns_%s.xlsx%n", config.getSystemName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workbook.close();
        }

        // Used to validate the ARs
        JsonArray jsonArray = toJsonArray(allARs);
        JsonReadWriteUtils.writeToJSON("./output/ArchRules.json", jsonArray);
    }

    private static void createIRSystem(String configPath, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(configPath);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    private static void detectAntipatterns(MicroserviceSystem currentSystem, Map<String, Integer> allAntiPatterns, Map<String, Double> metrics, List<AbstractAR> currARs) {

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);
        MethodDependencyGraph mdg = new MethodDependencyGraph(currentSystem);

        // KEYS must match columnLabels field
        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        if (!greedyMicroservices.getGreedyMicroservices().isEmpty()) {
            allAntiPatterns.put("Greedy Microservices", greedyMicroservices.numGreedyMicro());
        }

        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        if (!hublikeMicroservices.getHublikeMicroservices().isEmpty()) {
            allAntiPatterns.put("Hub-like Microservices", hublikeMicroservices.numHubLike());
        }

        ServiceChainMSLevelService chainService = new ServiceChainMSLevelService();
        ServiceChain allChains = chainService.getServiceChains(sdg);
        if (!allChains.getChain().isEmpty()) {
            allAntiPatterns.put("Service Chains (MS level)", allChains.numServiceChains());
        }

        ServiceChainMethodLevelService chainService2 = new ServiceChainMethodLevelService();
        ServiceChain allChains2 = chainService2.getServiceChains(mdg);
        if (!allChains2.getChain().isEmpty()) {
            allAntiPatterns.put("Service Chains (method level)", allChains2.numServiceChains());
        }

        WrongCutsService wrongCutsService = new WrongCutsService();
        WrongCuts wrongCuts = wrongCutsService.detectWrongCuts(currentSystem);
        if (!wrongCuts.getWrongCuts().isEmpty()) {
            allAntiPatterns.put("Wrong Cuts", wrongCuts.numWrongCuts());
        }

        CyclicDependencyMSLevelService cycles = new CyclicDependencyMSLevelService();
        CyclicDependency cycleDependencies = cycles.findCyclicDependencies(sdg);
        if (!cycleDependencies.getCycles().isEmpty()) {
            allAntiPatterns.put("Cyclic Dependencies (MS level)", cycleDependencies.numCyclicDep());
        }

        CyclicDependencyMethodLevelService cycles2 = new CyclicDependencyMethodLevelService();
        CyclicDependency cycleDependencies2 = cycles2.findCyclicDependencies(mdg);
        if (!cycleDependencies2.getCycles().isEmpty()) {
            allAntiPatterns.put("Cyclic Dependencies (Method level)", cycleDependencies2.numCyclicDep());
        }

        WobblyServiceInteractionService wobbly = new WobblyServiceInteractionService();
        WobblyServiceInteraction wobblyService = wobbly.findWobblyServiceInteractions(currentSystem);
        if (!wobblyService.getWobblyServiceInteractions().isEmpty()) {
            allAntiPatterns.put("Wobbly Service Interactions", wobblyService.numWobbblyService());
        }

        NoHealthcheckService noHealthCheckService = new NoHealthcheckService();
        NoHealthcheck noHealthCheck = noHealthCheckService.checkHealthcheck(currentSystem);
        if (!noHealthCheck.getnoHealthcheck().isEmpty()){
            allAntiPatterns.put("No Healthchecks", noHealthCheck.numNoHealthChecks());
        }

        NoApiGatewayService noApiGatewayService = new NoApiGatewayService();
        NoApiGateway noApiGateway = noApiGatewayService.checkforApiGateway(currentSystem);
        if (noApiGateway.getnoApiGateway()){
            allAntiPatterns.put("No API Gateway", noApiGateway.getBoolApiGateway());
        }

        if (!sdg.vertexSet().isEmpty()) {
            DegreeCoupling dc = new DegreeCoupling(sdg);
            metrics.put("maxAIS", (double) dc.getMaxAIS());
            metrics.put("avgAIS", dc.getAvgAIS());
            metrics.put("stdAIS", dc.getStdAIS());
            metrics.put("maxADS", (double) dc.getMaxADS());
            metrics.put("ADCS", dc.getADCS());
            metrics.put("stdADS", dc.getStdADS());
            metrics.put("maxACS", (double) dc.getMaxACS());
            metrics.put("avgACS", dc.getAvgACS());
            metrics.put("stdACS", dc.getStdACS());
            metrics.put("SCF", dc.getSCF());
            metrics.put("SIY", (double) dc.getSIY());
            StructuralCoupling sc = new StructuralCoupling(sdg);
            metrics.put("maxSC", sc.getMaxSC());
            metrics.put("avgSC", sc.getAvgSC());
            metrics.put("stdSC", sc.getStdSC());
            ConnectedComponentsModularity mod = new ConnectedComponentsModularity(sdg);
            metrics.put("SCCmodularity", mod.getModularity());
        }

        MetricResultCalculation cohesionMetrics = RunCohesionMetrics.calculateCohesionMetrics(currentSystem);
        metrics.put("maxSIDC", cohesionMetrics.getMax("ServiceInterfaceDataCohesion"));
        metrics.put("avgSIDC", cohesionMetrics.getAverage("ServiceInterfaceDataCohesion"));
        metrics.put("stdSIDC", cohesionMetrics.getStdDev("ServiceInterfaceDataCohesion"));
        metrics.put("maxSSIC", cohesionMetrics.getMax("StrictServiceImplementationCohesion"));
        metrics.put("avgSSIC", cohesionMetrics.getAverage("StrictServiceImplementationCohesion"));
        metrics.put("stdSSIC", cohesionMetrics.getStdDev("StrictServiceImplementationCohesion"));
        metrics.put("maxLOMLC", cohesionMetrics.getMax("LackOfMessageLevelCohesion"));
        metrics.put("avgLOMLC", cohesionMetrics.getAverage("LackOfMessageLevelCohesion"));
        metrics.put("stdLOMLC", cohesionMetrics.getStdDev("LackOfMessageLevelCohesion"));

        UCDetectionService ucDetectionService = new UCDetectionService(DELTA_PATH, OLD_IR_PATH, NEW_IR_PATH);
        currARs.addAll(ucDetectionService.scanDeltaUC());
        currARs.addAll(ucDetectionService.scanSystemUC());

    }

    private static void writeEmptyRow(XSSFSheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        for(int i = 0; i < columnLabels.length; i++) {
            row.createCell(i).setCellValue(0);
        }

    }

    private static void updateExcel(XSSFSheet sheet, String commitID, Map<String, Integer> allAntiPatterns, Map<String, Double> metrics, List<AbstractAR> currARs, int rowIndex) {
        writeEmptyRow(sheet, rowIndex);
        Row row = sheet.getRow(rowIndex);

        Cell commitIdCell = row.createCell(0);
        commitIdCell.setCellValue(commitID.substring(0, 7));

        int[] arcrules_counts = new int[ARCHRULES];
        Arrays.fill(arcrules_counts, 0);

        if (currARs != null && !currARs.isEmpty()) {
            for (AbstractAR archRule : currARs) {
                if (archRule instanceof AR3) {
                    arcrules_counts[0]++;
                } else if (archRule instanceof AR4) {
                    arcrules_counts[1]++;
                } else if (archRule instanceof AR6) {
                    arcrules_counts[2]++;
                } else if (archRule instanceof AR20) {
                    arcrules_counts[3]++;
                }
            }
        }
        for (int i = 0; i < ANTIPATTERNS; i++) {
            int offset = i + 1; // i + 1 because the first column is for commit ID
            Cell cell = row.getCell(offset);
            cell.setCellValue(allAntiPatterns.getOrDefault(columnLabels[offset], 0));
        }
        for (int i = 0; i < METRICS; i++) {
            int offset = i + 1 + ANTIPATTERNS; // first column is for commit ID + rest for anti-patterns
            Cell cell = row.getCell(offset);
            cell.setCellValue(metrics.getOrDefault(columnLabels[offset],0.0));
        }
        for (int i = 0; i < arcrules_counts.length; i++) {
            Cell cell = row.getCell(i + 1 + ANTIPATTERNS + METRICS); // first column is for commit ID + rest for anti-patterns+metrics
            cell.setCellValue(arcrules_counts[i]);
        }

    }

    public static JsonArray toJsonArray(List<List<AbstractAR>> archRulesList) {
        JsonArray outerArray = new JsonArray();
        
        for (List<AbstractAR> archRules : archRulesList) {
            JsonArray innerArray = new JsonArray();
            for (AbstractAR archRule : archRules) {
                innerArray.add(archRule.toJsonObject());
            }
            outerArray.add(innerArray);
        }
        
        return outerArray;
    }

}
