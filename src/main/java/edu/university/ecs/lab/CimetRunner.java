package edu.university.ecs.lab;


import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;

import java.util.Objects;


public class CimetRunner {

    /**
     * Main method for full report TODO adapt for case of already having the initial IR (the previous
     * merged/newIR) aka not running IRExtraction
     *
     */
    public static void main(String[] args) throws Exception {

//    if (args.length != 5) {
//      System.err.println(
//          "Required arguments /path/to/config/file <base branch> <base commit> <compare branch>"
//              + " <compare commit>");
//      return;
//    }
//
//    String configPath = args[0];
//    String baseBranch = args[1];
//    String baseCommit = args[2];
//    String compareBranch = args[3];
//    String compareCommit = args[4];
//
//    // RUN IR EXTRACTION
//    System.out.println("Starting IR Extraction...");
//    String[] IRExtractionArgs = {configPath, baseBranch, baseCommit};
//    IRExtraction.main(IRExtractionArgs);
//
//    // RUN DELTA
//    System.out.println("Starting Delta Extraction...");
//    String[] deltaArgs = {compareBranch, compareCommit, configPath};
//    DeltaExtractionRunner.main(deltaArgs);
//
//    // RUN IR MERGE
//    System.out.println("Starting IR Merge...");
//    String[] IRMergeArgs = {
//      FullCimetUtils.pathToIR, FullCimetUtils.pathToDelta, configPath, compareBranch, compareCommit
//    };
//    IRMergeRunner.main(IRMergeArgs);

        MicroserviceSystem microserviceSystem1 = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);
        MicroserviceSystem microserviceSystem2 = JsonReadWriteUtils.readFromJSON("./output/IRCompare.json", MicroserviceSystem.class);

        System.out.println(Objects.equals(microserviceSystem1, microserviceSystem2));
    }
}
