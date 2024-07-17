package edu.university.ecs.lab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.gson.JsonArray;
import java.util.*;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.detection.antipatterns.models.*;
import edu.university.ecs.lab.detection.antipatterns.services.*;
import edu.university.ecs.lab.detection.architecture.models.*;
import edu.university.ecs.lab.detection.architecture.services.UCDetectionService;
import edu.university.ecs.lab.detection.metrics.RunCohesionMetrics;
import edu.university.ecs.lab.detection.metrics.models.ConnectedComponentsModularity;
import edu.university.ecs.lab.detection.metrics.models.DegreeCoupling;
import edu.university.ecs.lab.detection.metrics.models.StructuralCoupling;
import edu.university.ecs.lab.detection.metrics.services.MetricResultCalculation;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;

public class AllConfigsExcelRunner {
    public static void main(String[] args) throws IOException {

        File configDir = new File("./configs");
        if (!configDir.exists() || !configDir.isDirectory()) {
            System.out.println("Config directory './configs' does not exist or is not a directory.");
            return;
        }

        File[] configFiles = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            System.out.println("No configuration files found in './configs' directory.");
            return;
        }

        for (File configFile : configFiles) {
            String configPath = configFile.getAbsolutePath();
            System.out.println("Processing config file: " + configPath);

            Config config = ConfigUtil.readConfig(configPath);
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
            createIRSystem(configPath, "OldIR.json");

            //Create excel file and desired header labels
            XSSFWorkbook workbook = new XSSFWorkbook();

            XSSFSheet sheet = workbook.createSheet(config.getSystemName());
            String[] columnLabels = {"Commit ID", "Greedy Microservices", "Hub-like Microservices", "Service Chains",
                "Wrong Cuts", "Cyclic Dependencies", "Wobbly Service Interactions", "No Healthchecks", "No API Gateway", "maxAIS",
                "avgAIS", "stdAIS", "maxADC", "ADCS", "stdADS", "maxACS", "avgACS", "stdACS", "SCF", "SIY", "maxSC", "avgSC",
                "stdSC", "SCCmodularity", "maxSIDC", "avgSIDC", "stdSIDC", "maxSSIC", "avgSSIC", "stdSSIC",
                "maxLOMLC", "avgLOMLC", "stdLOMLC", "AR3 (System)","AR4 (System)", "AR6 (Delta)", "AR20 (System)"};

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

                List<AntiPattern> allAntiPatterns = new ArrayList<>();
                HashMap<String, Double> metrics = new HashMap<>();
                List<AbstractAR> currARs = new ArrayList<>();

                MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON("./output/OldIR.json", MicroserviceSystem.class);

                if (!microserviceSystem.getMicroservices().isEmpty()) {
                    detectAntipatterns(allAntiPatterns, metrics);
                }

                // Extract changes from one commit to the other
                deltaExtractionService = new DeltaExtractionService(configPath, "./output/OldIR.json", commitIdOld, commitIdNew);
                deltaExtractionService.generateDelta();

                // Merge Delta changes to old IR to create new IR representing new commit changes
                MergeService mergeService = new MergeService("./output/OldIR.json", "./output/Delta.json", configPath);
                mergeService.generateMergeIR();

                UCDetectionService ucDetectionService = new UCDetectionService("./output/Delta.json", "./output/OldIR.json", "./output/NewIR.json");
                currARs.addAll(ucDetectionService.scanDeltaUC());
                currARs.addAll(ucDetectionService.scanSystemUC());


                if((i + 1) == 2) {
                    // On the first run we will write initial row to be empty and write the next row
                    writeEmptyRow(sheet, i);
                    writeToExcel(sheet, currARs, i + 1);
                } else if((i + 1) >= (list.size() - 1)) {
                    // If i+1 goes over we will write an empty row
                    writeEmptyRow(sheet, i);
                } else {
                    // Otherwise we will write the next row
                    writeToExcel(sheet, currARs, i + 1);
                }

                updateExcel(sheet, commitIdOld, allAntiPatterns, metrics, i);

                try {
                    Files.move(Paths.get("./output/NewIR.json"), Paths.get("./output/OldIR.json"), StandardCopyOption.REPLACE_EXISTING);
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
    }

    private static void createIRSystem(String configPath, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(configPath);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    private static void detectAntipatterns(List<AntiPattern> allAntiPatterns, Map<String, Double> metrics) {
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON("./output/OldIR.json", MicroserviceSystem.class);

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);

        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        if (!greedyMicroservices.getGreedyMicroservices().isEmpty()) {
            allAntiPatterns.add(greedyMicroservices);
        }

        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        if (!hublikeMicroservices.getHublikeMicroservices().isEmpty()) {
            allAntiPatterns.add(hublikeMicroservices);
        }

        ServiceChainService chainService = new ServiceChainService();
        ServiceChain allChains = chainService.getServiceChains(sdg);
        if (!allChains.getChain().isEmpty()) {
            allAntiPatterns.add(allChains);
        }

        WrongCutsService wrongCutsService = new WrongCutsService();
        WrongCuts wrongCuts = wrongCutsService.detectWrongCuts(currentSystem);
        if (!wrongCuts.getWrongCuts().isEmpty()) {
            allAntiPatterns.add(wrongCuts);
        }

        CyclicDependencyService cycles = new CyclicDependencyService();
        CyclicDependency cycleDependencies = cycles.findCyclicDependencies(sdg);
        if (!cycleDependencies.getCycles().isEmpty()) {
            allAntiPatterns.add(cycleDependencies);
        }

        WobblyServiceInteractionService wobbly = new WobblyServiceInteractionService();
        WobblyServiceInteraction wobblyService = wobbly.findWobblyServiceInteractions(currentSystem);
        if (!wobblyService.getWobblyServiceInteractions().isEmpty()) {
            allAntiPatterns.add(wobblyService);
        }

        NoHealthcheckService noHealthCheckService = new NoHealthcheckService();
        NoHealthcheck noHealthCheck = noHealthCheckService.checkHealthcheck(currentSystem);
        if (!noHealthCheck.getnoHealthcheck().isEmpty()){
            allAntiPatterns.add(noHealthCheck);
        }

        NoApiGatewayService noApiGatewayService = new NoApiGatewayService();
        NoApiGateway noApiGateway = noApiGatewayService.checkforApiGateway(currentSystem);
        if (noApiGateway.getnoApiGateway()){
            allAntiPatterns.add(noApiGateway);
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

        MetricResultCalculation cohesionMetrics = RunCohesionMetrics.calculateCohesionMetrics("./output/OldIR.json");
        metrics.put("maxSIDC", cohesionMetrics.getMax("ServiceInterfaceDataCohesion"));
        metrics.put("avgSIDC", cohesionMetrics.getAverage("ServiceInterfaceDataCohesion"));
        metrics.put("stdSIDC", cohesionMetrics.getStdDev("ServiceInterfaceDataCohesion"));
        metrics.put("maxSSIC", cohesionMetrics.getMax("StrictServiceImplementationCohesion"));
        metrics.put("avgSSIC", cohesionMetrics.getAverage("StrictServiceImplementationCohesion"));
        metrics.put("stdSSIC", cohesionMetrics.getStdDev("StrictServiceImplementationCohesion"));
        metrics.put("maxLOMLC", cohesionMetrics.getMax("LackOfMessageLevelCohesion"));
        metrics.put("avgLOMLC", cohesionMetrics.getAverage("LackOfMessageLevelCohesion"));
        metrics.put("stdLOMLC", cohesionMetrics.getStdDev("LackOfMessageLevelCohesion"));

    }

    private static void writeEmptyRow(XSSFSheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        for(int i = 0; i < 35; i++) {
            row.createCell(i).setCellValue(0);
        }

    }

    private static void writeToExcel(XSSFSheet sheet, List<AbstractAR> currARs, int rowIndex) {
        Row row = sheet.createRow(rowIndex);


        int[] arcrules_counts = new int[4];
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

        int[] antipattern_counts = new int[8]; // array to store the counts of each anti-pattern
        double[] metric_counts = new double[24];


        // Default value
        for (int i = 0; i < antipattern_counts.length; i++) {
            Cell cell = row.createCell(i + 1); // i + 1 because the first column is for commit ID
            cell.setCellValue(0);
        }
        // Default value
        for (int i = 0; i < metric_counts.length; i++) {
            Cell cell = row.createCell(i + 1 + antipattern_counts.length); // first column is for commit ID + rest for anti-patterns
            cell.setCellValue(0.0);
        }
        for (int i = 0; i < arcrules_counts.length; i++) {
            Cell cell = row.createCell(i + 1 + antipattern_counts.length + metric_counts.length); // first column is for commit ID + rest for anti-patterns
            cell.setCellValue(arcrules_counts[i]);
        }
    }

    private static void updateExcel(XSSFSheet sheet, String commitID, List<AntiPattern> allAntiPatterns, Map<String, Double> metrics, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        Cell commitIdCell = row.createCell(0);
        commitIdCell.setCellValue(commitID.substring(0, 7));

        int[] antipattern_counts = new int[8]; // array to store the counts of each anti-pattern
        Arrays.fill(antipattern_counts, 0);

        if (allAntiPatterns != null && !allAntiPatterns.isEmpty()) {
            for (AntiPattern antiPattern : allAntiPatterns) {
                if (antiPattern instanceof GreedyMicroservice) {
                    antipattern_counts[0] = ((GreedyMicroservice) antiPattern).numGreedyMicro();
                } else if (antiPattern instanceof HubLikeMicroservice) {
                    antipattern_counts[1] = ((HubLikeMicroservice) antiPattern).numHubLike();
                } else if (antiPattern instanceof ServiceChain) {
                    antipattern_counts[2] = ((ServiceChain) antiPattern).numServiceChains();
                } else if (antiPattern instanceof WrongCuts) {
                    antipattern_counts[3] = ((WrongCuts) antiPattern).numWrongCuts();
                } else if (antiPattern instanceof CyclicDependency) {
                    antipattern_counts[4] = ((CyclicDependency) antiPattern).numCyclicDep();
                } else if (antiPattern instanceof WobblyServiceInteraction) {
                    antipattern_counts[5] = ((WobblyServiceInteraction) antiPattern).numWobbblyService();
                } else if (antiPattern instanceof NoHealthcheck){
                    antipattern_counts[6] = ((NoHealthcheck) antiPattern).numNoHealthChecks();
                } else if (antiPattern instanceof NoApiGateway){
                    antipattern_counts[7] = ((NoApiGateway) antiPattern).getBoolApiGateway();
                }
            }
        }

        double[] metric_counts = new double[24];
        metric_counts[0] = metrics.getOrDefault("maxAIS", 0.0);
        metric_counts[1] = metrics.getOrDefault("avgAIS", 0.0);
        metric_counts[2] = metrics.getOrDefault("stdAIS", 0.0);
        metric_counts[3] = metrics.getOrDefault("maxADS", 0.0);
        metric_counts[4] = metrics.getOrDefault("ADCS", 0.0);
        metric_counts[5] = metrics.getOrDefault("stdADS", 0.0);
        metric_counts[6] = metrics.getOrDefault("maxACS", 0.0);
        metric_counts[7] = metrics.getOrDefault("avgACS", 0.0);
        metric_counts[8] = metrics.getOrDefault("stdACS", 0.0);
        metric_counts[9] = metrics.getOrDefault("SCF", 0.0);
        metric_counts[10] = metrics.getOrDefault("SIY", 0.0);
        metric_counts[11] = metrics.getOrDefault("maxSC", 0.0);
        metric_counts[12] = metrics.getOrDefault("avgSC", 0.0);
        metric_counts[13] = metrics.getOrDefault("stdSC", 0.0);
        metric_counts[14] = metrics.getOrDefault("SCCmodularity", 0.0);
        metric_counts[15] = metrics.getOrDefault("maxSIDC", 0.0);
        metric_counts[16] = metrics.getOrDefault("avgSIDC", 0.0);
        metric_counts[17] = metrics.getOrDefault("stdSIDC", 0.0);
        metric_counts[18] = metrics.getOrDefault("maxSSIC", 0.0);
        metric_counts[19] = metrics.getOrDefault("avgSSIC", 0.0);
        metric_counts[20] = metrics.getOrDefault("stdSSIC", 0.0);
        metric_counts[21] = metrics.getOrDefault("maxLOMLC", 0.0);
        metric_counts[22] = metrics.getOrDefault("avgLOMLC", 0.0);
        metric_counts[23] = metrics.getOrDefault("stdLOMLC", 0.0);

        for (int i = 0; i < antipattern_counts.length; i++) {
            Cell cell = row.getCell(i + 1); // i + 1 because the first column is for commit ID
            cell.setCellValue(antipattern_counts[i]);
        }
        for (int i = 0; i < metric_counts.length; i++) {
            Cell cell = row.getCell(i + 1 + antipattern_counts.length); // first column is for commit ID + rest for anti-patterns
            cell.setCellValue(metric_counts[i]);
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
