package edu.university.ecs.lab.report;

import java.io.*;
import java.util.*;

/** Testing for report execution, {@link ReportService} contains all logic */
public class ReportRunner {

  /**
   * Main method for generating a report by running metrics and report generation.
   *
   * @param args <base branch> <base commit> <compare branch> <compare commit>
   *     <path/to/intermediate-json> <path/to/new-intermediate-json> <path/to/delta-json>
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 7) {
      System.err.println(
          "Invalid # of args, 2 expected: <base branch> <base commit> <compare branch> <compare"
              + " commit> <path/to/intermediate-json> <path/to/new-intermediate-json>"
              + " <path/to/delta-json>");
      return;
    }

    String baseBranch = args[0];
    String baseCommit = args[1];
    String compareBranch = args[2];
    String compareCommit = args[3];
    String intermediatePath = args[4];
    String newIntermediatePath = args[5];
    String deltaPath = args[6];

    ReportService reportService =
        new ReportService(
            baseBranch,
            baseCommit,
            compareBranch,
            compareCommit,
            intermediatePath,
            newIntermediatePath,
            deltaPath);

    reportService.generateReport();
  }
}
