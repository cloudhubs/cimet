//package edu.university.ecs.lab;
//
//import edu.university.ecs.lab.common.config.ConfigUtil;
//import edu.university.ecs.lab.common.config.Config;
//import edu.university.ecs.lab.common.services.GitService;
//import edu.university.ecs.lab.delta.services.DeltaExtractionService;
//import edu.university.ecs.lab.intermediate.create.IRExtractionRunner;
//import org.eclipse.jgit.revwalk.RevCommit;
//import java.util.List;
//
//
//public class IncrementalCimetRunner {
//
//
//  /**
//   * Main method for full incremental report
//   *
//   * @param args /path/to/config/file <base branch> <base commit> <compare commit>
//   */
//    public static void main(String[] args) throws Exception {
//        args = new String[]{"./config.json"};
//
//        // Run IR Extraction on the base commit
//        IRExtractionRunner.main(args);
//
//
//        // Generate deltas from latest down to base
//        Config config = ConfigUtil.readConfig(args[0]);
//        GitService gitService = new GitService(args[0]);
//        Iterable<RevCommit> revCommits = gitService.getLog();
//        DeltaExtractionService deltaExtractionService;
//
////        for(int i = 0; i < revCommits.)
//    }
//
//}
