// package edu.university.ecs.lab.intermediate.merge;
//
// import edu.university.ecs.lab.common.config.ConfigUtil;
// import edu.university.ecs.lab.common.config.Config;
// import edu.university.ecs.lab.common.error.Error;
// import edu.university.ecs.lab.intermediate.merge.services.MergeService;
//
// import java.io.IOException;
//
// public class IRMergeRunner {
//
//  /**
//   * Entry point for the intermediate representation merge process.
//   *
//   * @param args {@literal </path/to/intermediate-json> </path/to/delta-json> </path/to/config>}
//   *     {@literal <compare branch> <compare commit>}
//   */
//  public static void main(String[] args) throws IOException {
//      args = new String[]{"./config.json"};
//      if (args.length != 1) {
//          Error.reportAndExit(Error.INVALID_ARGS);
//      }
//
//    Config config = ConfigUtil.readConfig(args[0]);
//
//    MergeService mergeService = new MergeService(args[0]);
//    String outputFileName = mergeService.mergeAndWriteToFile();
//  }
// }
