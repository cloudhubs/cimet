//package edu.university.ecs.lab.delta;
//
//import edu.university.ecs.lab.common.config.ConfigUtil;
//import edu.university.ecs.lab.common.config.Config;
//import edu.university.ecs.lab.common.error.Error;
//import edu.university.ecs.lab.delta.services.DeltaExtractionService;
//
//import java.util.*;
//
///**
// * Service for extracting the differences between a local and remote repository. TODO: notice how
// * {@link DeltaExtractionService#generateDelta()} returns a set of file names, we should make this
// * all 1 file for the multi-repo case.
// */
//public class DeltaExtractionRunner {
//  /**
//   * Compares the branch specified in the Rest Extraction file to a commit on the remote repository
//   * branch name specified in the arguments and generates the delta file.
//   *
//   * @param args {@literal <compareBranch> <compareCommit> [/path/to/config]}
//   */
//  public static void main(String[] args) throws Exception {
////      args = new String[]{"./config.json"};
//      if (args.length != 3) {
//          Error.reportAndExit(Error.INVALID_ARGS);
//      }
//
//    DeltaExtractionService deltaService = new DeltaExtractionService(args[0], args[1], args[2]);
//
//    deltaService.generateDelta();
//
//  }
//}
