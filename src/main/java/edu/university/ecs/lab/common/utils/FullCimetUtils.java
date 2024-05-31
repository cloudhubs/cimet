//package edu.university.ecs.lab.common.utils;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Getter
//@Setter
//public class FullCimetUtils {
//
//  public static String pathToIR;
//  public static String pathToDelta;
//  public static String pathToNewIR;
//  public static Set<String> microservicePaths = new HashSet<>();
//
//  public static String baseBranch = "";
//  public static String baseCommit = "";
//  private static final int SHORT_COMMIT_LENGTH = 6;
//
//  // TODO make output dir based on config
//  public static String getDeltaOutputName(String branch, String compareCommit) {
//    String outputName =
//        "./out/delta-changes-["
//            + FullCimetUtils.baseBranch
//            + "-"
//            + getShortCommit(FullCimetUtils.baseCommit)
//            + "-to-"
//            + branch
//            + "-"
//            + compareCommit.substring(0, 7)
//            + "].json";
//    return outputName;
//  }
//
//  /**
//   * Get the short commit hash from the full hash. Safe for null/empty
//   *
//   * @param commit the full commit hash
//   * @return the short commit hash, or empty string if null/empty
//   */
//  public static String getShortCommit(String commit) {
//    if (commit == null || commit.isEmpty()) {
//      return "";
//    }
//
//    return commit.length() > SHORT_COMMIT_LENGTH
//        ? commit.substring(0, SHORT_COMMIT_LENGTH + 1)
//        : commit;
//  }
//}
