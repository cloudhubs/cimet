package edu.university.ecs.lab.detection;

import com.google.gson.JsonArray;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.MethodDependencyGraph;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.detection.antipatterns.services.*;
import edu.university.ecs.lab.detection.architecture.models.*;
import edu.university.ecs.lab.detection.architecture.services.ARDetectionService;
import edu.university.ecs.lab.detection.metrics.RunCohesionMetrics;
import edu.university.ecs.lab.detection.metrics.models.ConnectedComponentsModularity;
import edu.university.ecs.lab.detection.metrics.models.DegreeCoupling;
import edu.university.ecs.lab.detection.metrics.models.StructuralCoupling;
import edu.university.ecs.lab.detection.metrics.services.MetricResultCalculation;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class DetectionService {
    private static final String[] columnLabels = new String[]{"Commit ID", "Greedy Microservices", "Hub-like Microservices", "Service Chains (MS level)", "Service Chains (Method level)",
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

    private final String configPath;
    private final Config config;
    private final GitService gitService;
    private IRExtractionService irExtractionService;
    private ARDetectionService arDetectionService;
    private DeltaExtractionService deltaExtractionService;
    private MergeService mergeService;
    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
//    private final String firstCommitID

    public DetectionService(String configPath) {
        this.configPath = configPath;
        // Read in config
        config = ConfigUtil.readConfig(configPath);
        // Setup dirs
        FileUtils.makeDirs();
        // Setup local repo
        gitService = new GitService(configPath);
        workbook = new XSSFWorkbook();
    }

    public void runDetection() {

        // Get list of commits
        Iterable<RevCommit> iterable = gitService.getLog();
        List<RevCommit> commits = iterableToList(iterable);

        // Generate the initial IR
        irExtractionService = new IRExtractionService(configPath, Optional.of(commits.get(0).toString().split(" ")[1]));
        irExtractionService.generateIR("OldIR.json");

        // Setup sheet and headers
        sheet = workbook.createSheet(config.getSystemName());
        writeHeaders();

        // Write the initial row as empty
        writeEmptyRow(1);

        // Starting at the first commit until commits - 1
        for (int i = 0; i < commits.size(); i++) {
            MicroserviceSystem newSystem = null;
            SystemChange systemChange = null;

            // Old commit = curr, new commit = next
            String commitIdOld = commits.get(i).toString().split(" ")[1];

            int currIndex = i + 1, nextIndex = i + 2;

            // Fill the next row as empty for future use
            if(i < commits.size() - 1) {
                writeEmptyRow(nextIndex);
            }

            // Get instance of our current row
            Row row = sheet.getRow(currIndex);

            // Set the commitID as the first cell value
            Cell commitIdCell = row.createCell(0);
            commitIdCell.setCellValue(commitIdOld.substring(0, 7));

            // Read in the old system
            MicroserviceSystem oldSystem = JsonReadWriteUtils.readFromJSON(OLD_IR_PATH, MicroserviceSystem.class);

            // Extract changes from one commit to the other
            if(i < commits.size() - 1) {
                String commitIdNew = commits.get(i + 1).toString().split(" ")[1];

                deltaExtractionService = new DeltaExtractionService(configPath, OLD_IR_PATH, commitIdOld, commitIdNew);
                deltaExtractionService.generateDelta();

                // Merge Delta changes to old IR to create new IR representing new commit changes
                MergeService mergeService = new MergeService(OLD_IR_PATH, DELTA_PATH, configPath);
                mergeService.generateMergeIR();

                // Read in the new system and system change
                newSystem = JsonReadWriteUtils.readFromJSON(NEW_IR_PATH, MicroserviceSystem.class);
                systemChange = JsonReadWriteUtils.readFromJSON(DELTA_PATH, SystemChange.class);
            }

            // Init all the lists/maps
            List<AbstractAR> rules = new ArrayList<>();
            Map<String, Integer> antipatterns = new HashMap<>();
            HashMap<String, Double> metrics = new HashMap<>();

            // We can detect/update if there are >= 1 microservices
            if (Objects.nonNull(oldSystem.getMicroservices()) && !oldSystem.getMicroservices().isEmpty()) {
                detectAntipatterns(oldSystem, antipatterns);
                detectMetrics(oldSystem, metrics);

                updateAntiPatterns(currIndex, antipatterns);
                updateMetrics(currIndex, metrics);

                // For simplicity we will skip rules on the last iteration since there is no newSystem
                if(i < commits.size() - 1) {
                    arDetectionService = new ARDetectionService(systemChange, oldSystem, newSystem);
                    rules = arDetectionService.scanUseCases();

                    updateRules(nextIndex, rules);
                } else {
                    continue;
                }
            }

            // After completing this iteration, we can replace oldIR with newIR
            try {
                Files.move(Paths.get(NEW_IR_PATH), Paths.get(OLD_IR_PATH), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // At the end we write the workbook to file
        try (FileOutputStream fileOut = new FileOutputStream(String.format("./output/AntiPatterns_%s.xlsx", config.getSystemName()))) {
            workbook.write(fileOut);
            System.out.printf("Excel file created: AntiPatterns_%s.xlsx%n", config.getSystemName());
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);

        }

    }


    private void writeHeaders() {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnLabels.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnLabels[i]);
        }
    }

    private void writeEmptyRow(int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        for(int i = 0; i < columnLabels.length; i++) {
            row.createCell(i).setCellValue(0);
        }

    }

    private List<RevCommit> iterableToList(Iterable<RevCommit> iterable) {
        Iterator<RevCommit> iterator = iterable.iterator();
        List<RevCommit> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.reverse(list);

        return list;
    }

    private void detectAntipatterns(MicroserviceSystem microserviceSystem, Map<String, Integer> allAntiPatterns) {

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(microserviceSystem);
        MethodDependencyGraph mdg = new MethodDependencyGraph(microserviceSystem);

        // KEYS must match columnLabels field
        allAntiPatterns.put("Greedy Microservices", new GreedyService().getGreedyMicroservices(sdg).numGreedyMicro());
        allAntiPatterns.put("Hub-like Microservices", new HubLikeService().getHubLikeMicroservice(sdg).numHubLike());
        allAntiPatterns.put("Service Chains (MS level)", new ServiceChainMSLevelService().getServiceChains(sdg).numServiceChains());
        allAntiPatterns.put("Service Chains (Method level)", new ServiceChainMethodLevelService().getServiceChains(mdg).numServiceChains());
        allAntiPatterns.put("Wrong Cuts", new WrongCutsService().detectWrongCuts(microserviceSystem).numWrongCuts());
        allAntiPatterns.put("Cyclic Dependencies (MS level)", new CyclicDependencyMSLevelService().findCyclicDependencies(sdg).numCyclicDep());
        allAntiPatterns.put("Cyclic Dependencies (Method level)", new CyclicDependencyMethodLevelService().findCyclicDependencies(mdg).numCyclicDep());
        allAntiPatterns.put("Wobbly Service Interactions", new WobblyServiceInteractionService().findWobblyServiceInteractions(microserviceSystem).numWobbblyService());
        allAntiPatterns.put("No Healthchecks", new NoHealthcheckService().checkHealthcheck(microserviceSystem).numNoHealthChecks());
        allAntiPatterns.put("No API Gateway", new NoApiGatewayService().checkforApiGateway(microserviceSystem).getBoolApiGateway());

    }

    private void detectMetrics(MicroserviceSystem microserviceSystem, Map<String, Double> metrics) {

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(microserviceSystem);
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

    private void updateRules(int rowIndex, List<AbstractAR> currARs) {
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

        Row row = sheet.getRow(rowIndex);
        for (int i = 0; i < arcrules_counts.length; i++) {
            Cell cell = row.getCell(i + 1 + ANTIPATTERNS + METRICS); // first column is for commit ID + rest for anti-patterns+metrics
            cell.setCellValue(arcrules_counts[i]);
        }
    }

    private void updateAntiPatterns(int rowIndex, Map<String, Integer> allAntiPatterns) {
        Row row = sheet.getRow(rowIndex);

        for (int i = 0; i < ANTIPATTERNS; i++) {
            int offset = i + 1; // i + 1 because the first column is for commit ID
            Cell cell = row.getCell(offset);

            // Default value for No API Gateway is 1, meaning true
            cell.setCellValue(
                    allAntiPatterns.getOrDefault(
                            columnLabels[offset],
                            "No API Gateway".equals(columnLabels[offset]) ? 1 : 0
                    ));
        }
    }

    private void updateMetrics(int rowIndex, Map<String, Double> metrics) {
        Row row = sheet.getRow(rowIndex);

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
