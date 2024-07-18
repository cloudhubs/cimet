package edu.university.ecs.lab.detection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import edu.university.ecs.lab.delta.models.SystemChange;
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
        createIRSystem(config, "OldIR.json");

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

        for (int i = 0; i < list.size() - 1; i++) {
            String commitIdOld = list.get(i).toString().split(" ")[1];
            String commitIdNew = list.get(i + 1).toString().split(" ")[1];
            writeEmptyRow(sheet, i + 1);
            Row row = sheet.getRow(i + 1);

            Cell commitIdCell = row.createCell(0);
            commitIdCell.setCellValue(commitIdOld.substring(0, 7));

            Map<String, Integer> allAntiPatterns = new HashMap<>();
            HashMap<String, Double> metrics = new HashMap<>();

            MicroserviceSystem microserviceSystem = JsonReadWriteUtils.readFromJSON(OLD_IR_PATH, MicroserviceSystem.class);

            // Extract changes from one commit to the other
            deltaExtractionService = new DeltaExtractionService(config_path, OLD_IR_PATH, commitIdOld, commitIdNew);
            deltaExtractionService.generateDelta();

            // Merge Delta changes to old IR to create new IR representing new commit changes
            MergeService mergeService = new MergeService(OLD_IR_PATH, DELTA_PATH, config_path);
            mergeService.generateMergeIR();

            MicroserviceSystem microserviceSystemNew = JsonReadWriteUtils.readFromJSON(NEW_IR_PATH, MicroserviceSystem.class);
            SystemChange oldSystem = JsonReadWriteUtils.readFromJSON(DELTA_PATH, SystemChange.class);

            if (!microserviceSystem.getMicroservices().isEmpty()) {
                detectAntipatterns(microserviceSystem,allAntiPatterns, metrics);
            }

            ARDetectionService ucDetectionService = new ARDetectionService(oldSystem, microserviceSystem, microserviceSystemNew);
            List<AbstractAR> currARs = ucDetectionService.scanUseCases();
            allARs.add(currARs);

            updateAntiPatterns(row, allAntiPatterns);
            
            if (!metrics.isEmpty()) {
                updateMetrics(row, metrics);
            }
            if (!currARs.isEmpty()) {
                updateAR(row, currARs);
            }

            try {
                Files.move(Paths.get(NEW_IR_PATH), Paths.get(OLD_IR_PATH), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private static void createIRSystem(Config config, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(config);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    private static void detectAntipatterns(MicroserviceSystem currentSystem, Map<String, Integer> allAntiPatterns, Map<String, Double> metrics) {

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);
        MethodDependencyGraph mdg = new MethodDependencyGraph(currentSystem);

        // KEYS must match columnLabels field
        allAntiPatterns.put("Greedy Microservices", new GreedyService().getGreedyMicroservices(sdg).numGreedyMicro());
        allAntiPatterns.put("Hub-like Microservices", new HubLikeService().getHubLikeMicroservice(sdg).numHubLike());
        allAntiPatterns.put("Service Chains (MS level)", new ServiceChainMSLevelService().getServiceChains(sdg).numServiceChains());
        allAntiPatterns.put("Service Chains (method level)", new ServiceChainMethodLevelService().getServiceChains(mdg).numServiceChains());
        allAntiPatterns.put("Wrong Cuts", new WrongCutsService().detectWrongCuts(currentSystem).numWrongCuts());
        allAntiPatterns.put("Cyclic Dependencies (MS level)", new CyclicDependencyMSLevelService().findCyclicDependencies(sdg).numCyclicDep());
        allAntiPatterns.put("Cyclic Dependencies (Method level)", new CyclicDependencyMethodLevelService().findCyclicDependencies(mdg).numCyclicDep());
        allAntiPatterns.put("Wobbly Service Interactions", new WobblyServiceInteractionService().findWobblyServiceInteractions(currentSystem).numWobbblyService());
        allAntiPatterns.put("No Healthchecks", new NoHealthcheckService().checkHealthcheck(currentSystem).numNoHealthChecks());
        allAntiPatterns.put("No API Gateway", new NoApiGatewayService().checkforApiGateway(currentSystem).getBoolApiGateway());

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

        MetricResultCalculation cohesionMetrics = RunCohesionMetrics.calculateCohesionMetrics(OLD_IR_PATH);
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
        for(int i = 0; i < columnLabels.length; i++) {
            row.createCell(i).setCellValue(0);
        }

    }

    private static void updateAR(Row row, List<AbstractAR> currARs) {
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
        for (int i = 0; i < arcrules_counts.length; i++) {
            Cell cell = row.getCell(i + 1 + ANTIPATTERNS + METRICS); // first column is for commit ID + rest for anti-patterns+metrics
            cell.setCellValue(arcrules_counts[i]);
        }
    }

    private static void updateAntiPatterns(Row row, Map<String, Integer> allAntiPatterns) {
        for (int i = 0; i < ANTIPATTERNS; i++) {
            int offset = i + 1; // i + 1 because the first column is for commit ID
            Cell cell = row.getCell(offset);
            cell.setCellValue(
                allAntiPatterns.getOrDefault(
                    columnLabels[offset], 
                    "No API Gateway".equals(columnLabels[offset]) ? 1 : 0
                ));
        }
    }

    private static void updateMetrics(Row row, Map<String, Double> metrics) {
        for (int i = 0; i < METRICS; i++) {
            int offset = i + 1 + ANTIPATTERNS; // first column is for commit ID + rest for anti-patterns
            Cell cell = row.getCell(offset);
            cell.setCellValue(metrics.getOrDefault(columnLabels[offset],0.0));
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
