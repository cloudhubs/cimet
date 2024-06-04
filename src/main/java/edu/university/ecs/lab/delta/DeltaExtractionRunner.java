//package edu.university.ecs.lab.delta;
//
//import edu.university.ecs.lab.common.config.ConfigUtil;
//import edu.university.ecs.lab.common.config.Config;
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
//
//
//    DeltaExtractionService deltaService =
//        new DeltaExtractionService(branch, compareCommit, config);
//    List<String> outputNames = deltaService.generateDelta();
//
//  }
//}
