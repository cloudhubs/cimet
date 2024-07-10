package edu.university.ecs.lab.detection;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import edu.university.ecs.lab.detection.architecture.models.AbstractAR;
import edu.university.ecs.lab.detection.architecture.models.*;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.gson.JsonArray;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.detection.architecture.services.UCDetectionService;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class UCDetectionRunner {
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
        config.setBaseCommit(list.get(1).toString().split(" ")[1]);
        // Create IR of first commit
        createIRSystem(config, "OldIR.json");

        List<JsonObject> allUseCases = new ArrayList<>();

        // Loop through commit history and create delta, merge, etc...
        for (int i = 0; i < list.size() - 1; i++) {
            String commitIdOld = list.get(i).toString().split(" ")[1];
            String commitIdNew = list.get(i + 1).toString().split(" ")[1];

            // Extract changes from one commit to the other
            deltaExtractionService = new DeltaExtractionService("./config.json", "./output/OldIR.json", commitIdOld, commitIdNew);
            deltaExtractionService.generateDelta();

            // Merge Delta changes to old IR to create new IR representing new commit changes
            MergeService mergeService = new MergeService("./output/OldIR.json", "./output/Delta.json", "./config.json");
            mergeService.generateMergeIR();
            //computeGraph("./output/rest-extraction-output-[main-" + commitIdNew.substring(0,7) + "].json", commitIdNew.substring(0,7));
        
            UCDetectionService ucDetectionService = new UCDetectionService("./output/Delta.json", "./output/OldIR.json", "./output/NewIR.json");
            List<AbstractAR> useCases = ucDetectionService.scanUseCases();
            JsonObject obj = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            jsonArray.addAll(JsonSerializable.toJsonArray(useCases));

            obj.addProperty("commitID", commitIdNew);
//            obj.addProperty("UseCase1", useCases.stream().filter(uc -> uc instanceof UseCase1).count());
            obj.addProperty("Architectural Rule 3", useCases.stream().filter(uc -> uc instanceof AR3).count());
            obj.addProperty("Architectural Rule 4", useCases.stream().filter(uc -> uc instanceof AR4).count());
            obj.addProperty("Architectural Rule 6", useCases.stream().filter(uc -> uc instanceof AR6).count());
            obj.addProperty("Architectural Rule 20", useCases.stream().filter(uc -> uc instanceof AR20).count());
//            obj.addProperty("UseCase6", useCases.stream().filter(uc -> uc instanceof UseCase20).count());
//            obj.addProperty("UseCase7", useCases.stream().filter(uc -> uc instanceof UseCase21).count());

            obj.add("Architectural Rules", jsonArray);
            allUseCases.add(obj);

            try {
                Files.move(Paths.get("./output/NewIR.json"), Paths.get("./output/OldIR.json"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonReadWriteUtils.writeToJSON("./output/ArchRules.json", allUseCases);
    }


    private static void createIRSystem(Config config, String fileName) {
        // Create both directories needed
        FileUtils.createPaths();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(config);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
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
