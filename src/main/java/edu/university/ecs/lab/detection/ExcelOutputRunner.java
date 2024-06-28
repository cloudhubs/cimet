package edu.university.ecs.lab.detection;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.jgit.revwalk.RevCommit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

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
import edu.university.ecs.lab.detection.architecture.models.AbstractUseCase;
import edu.university.ecs.lab.detection.architecture.services.UCDetectionService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;

import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelOutputRunner {
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
        config.setBaseCommit(list.get(0).toString().split(" ")[1]);
        // Create IR of first commit
        createIRSystem(config, "OldIR.json");

        List<List<AbstractUseCase>> allUseCases = new ArrayList<>();

        //Create excel file and desired header labels
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("Train-Ticket-Test");
        String[] columnLabels = {"Commit ID", "Greedy Micorservices", "Hub-like Microservices", "Service Chains", 
                                    "Wrong Cuts", "Cylic Dependencies", "Wobbly Service Interactions"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnLabels.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnLabels[i]);
        }

        for (int i = 0; i < list.size() - 1; i++) {
            String commitIdOld = list.get(i).toString().split(" ")[1];
            String commitIdNew = list.get(i + 1).toString().split(" ")[1];

            List<AntiPattern> allAntiPatterns = new ArrayList<>();

            try {
                Gson gson = new Gson();
                MicroserviceSystem microserviceSystem = gson.fromJson(new FileReader("./output/OldIR.json"), MicroserviceSystem.class);
                
                if (!microserviceSystem.getMicroservices().isEmpty()) {
                    detectAntipatterns("./output/OldIR.json", allAntiPatterns);
                }
            } catch (IOException e){
                e.printStackTrace();
            }

            writeToExcel(workbook, sheet, commitIdOld, allAntiPatterns, i + 1);

            // Extract changes from one commit to the other
            deltaExtractionService = new DeltaExtractionService("./config.json", "./output/OldIR.json", commitIdOld, commitIdNew);
            deltaExtractionService.generateDelta();

            // Merge Delta changes to old IR to create new IR representing new commit changes
            MergeService mergeService = new MergeService("./output/OldIR.json", "./output/Delta.json", "./config.json");
            mergeService.generateMergeIR();
            //computeGraph("./output/rest-extraction-output-[main-" + commitIdNew.substring(0,7) + "].json", commitIdNew.substring(0,7));
        
            UCDetectionService ucDetectionService = new UCDetectionService("./output/Delta.json", "./output/OldIR.json", "./output/NewIR.json");
            allUseCases.add(ucDetectionService.scanDelta());

            try {
                Files.move(Paths.get("./output/NewIR.json"), Paths.get("./output/OldIR.json"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try (FileOutputStream fileOut = new FileOutputStream("./output/AntiPatterns.xlsx")) {
            workbook.write(fileOut);
            workbook.close();
            System.out.println("Excel file created: AntiPatterns.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonArray jsonArray = toJsonArray(allUseCases);
        JsonReadWriteUtils.writeToJSON("./output/UseCase.json", jsonArray);        
    }

    private static void createIRSystem(Config config, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(config);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }

    //Need to implement NoAPI & Healthcheck -> need yaml in IR
    private static void detectAntipatterns(String IRPath, List<AntiPattern> allAntiPatterns){
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON(IRPath, MicroserviceSystem.class);

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);

        GreedyService greedy = new GreedyService();
        GreedyMicroservice greedyMicroservices = greedy.getGreedyMicroservices(sdg);
        if (!greedyMicroservices.getGreedyMicroservices().isEmpty()){
            allAntiPatterns.add(greedyMicroservices);
        }

        HubLikeService hublike = new HubLikeService();
        HubLikeMicroservice hublikeMicroservices = hublike.getHubLikeMicroservice(sdg);
        if (!hublikeMicroservices.getHublikeMicroservices().isEmpty()){
            allAntiPatterns.add(hublikeMicroservices);
        }

        ServiceChainService chainService = new ServiceChainService();
        ServiceChain allChains = chainService.getServiceChains(sdg);
        if (!allChains.getChain().isEmpty()){
            allAntiPatterns.add(allChains);
        }

        WrongCutsService wrongCutsService = new WrongCutsService();
        WrongCuts wrongCuts = wrongCutsService.detectWrongCuts(currentSystem);
        if (!wrongCuts.getWrongCuts().isEmpty()){
            allAntiPatterns.add(wrongCuts);
        }

        CyclicDependencyService cycles = new CyclicDependencyService();
        CyclicDependency cycleDependencies = cycles.findCyclicDependencies(sdg);
        if (!cycleDependencies.getCycles().isEmpty()){
            allAntiPatterns.add(cycleDependencies);
        }

        WobblyServiceInteractionService wobbly = new WobblyServiceInteractionService();
        WobblyServiceInteraction wobblyService = wobbly.findWobblyServiceInteractions(currentSystem);
        if (!wobblyService.getWobblyServiceInteractions().isEmpty()){
            allAntiPatterns.add(wobblyService);
        }
    }

    private static void writeToExcel(XSSFWorkbook workbook, XSSFSheet sheet, String commitID, List<AntiPattern> allAntiPatterns, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        Cell commitIdCell = row.createCell(0);
        commitIdCell.setCellValue(commitID.substring(0, 7));

        int[] counts = new int[6]; // array to store the counts of each anti-pattern
        Arrays.fill(counts, 0);

        if (allAntiPatterns != null && !allAntiPatterns.isEmpty()) {
            for (AntiPattern antiPattern : allAntiPatterns) {
                if (antiPattern instanceof GreedyMicroservice) {
                    counts[0] = ((GreedyMicroservice) antiPattern).numGreedyMicro();
                } else if (antiPattern instanceof HubLikeMicroservice) {
                    counts[1] = ((HubLikeMicroservice) antiPattern).numHubLike();
                } else if (antiPattern instanceof ServiceChain) {
                    counts[2] = ((ServiceChain) antiPattern).numServiceChains();
                } else if (antiPattern instanceof WrongCuts) {
                    counts[3] = ((WrongCuts) antiPattern).numWrongCuts();
                } else if (antiPattern instanceof CyclicDependency) {
                    counts[4] = ((CyclicDependency) antiPattern).numCyclicDep();
                } else if (antiPattern instanceof WobblyServiceInteraction) {
                    counts[5] = ((WobblyServiceInteraction) antiPattern).numWobbblyService();
                }
            }
        }

        for (int i = 0; i < counts.length; i++) {
            Cell cell = row.createCell(i + 1); // i + 1 because the first column is for commit ID
            cell.setCellValue(counts[i]);
        }
    }

    private static JsonArray toJsonArray(List<List<AbstractUseCase>> useCaseLists) {
        JsonArray outerArray = new JsonArray();
        
        for (List<AbstractUseCase> useCaseList : useCaseLists) {
            JsonArray innerArray = new JsonArray();
            for (AbstractUseCase useCase : useCaseList) {
                innerArray.add(useCase.toJsonObject());
            }
            outerArray.add(innerArray);
        }
        
        return outerArray;
    }
    
}
