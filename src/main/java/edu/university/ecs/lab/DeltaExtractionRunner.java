//package edu.university.ecs.lab.delta;
//
//import edu.university.ecs.lab.delta.services.DeltaExtractionService;
//
///**
// * This class acts as a runner implementation for extracting a Delta file
// */
//public class DeltaExtractionRunner {
//    /**
//     * This method compares two commits on the specified branch in the config
//     *
//     * @param args {@literal [/path/to/config] <oldCommit> <newCommit> }
//     */
//    public static void main(String[] args) throws Exception {
//        args = new String[]{"./config.json", "922ac448a5b404d16190a9740984251626b2d70e", "300b9ee50aaca00c3b2d02c85e350d295aa4fa0a"};
//        String[] finalArgs = args;
//
//        DeltaExtractionService deltaService = new DeltaExtractionService(args[0], "./output/OldIR.json", args[1], args[2]);
//
//        deltaService.generateDelta();
//
//    }
//}
//