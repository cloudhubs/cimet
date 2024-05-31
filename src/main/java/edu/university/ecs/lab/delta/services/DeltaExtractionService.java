package edu.university.ecs.lab.delta.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.models.InputRepository;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FullCimetUtils;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.utils.GitFetchUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static edu.university.ecs.lab.common.models.enums.ErrorCodes.DELTA_EXTRACTION_FAIL;
import static edu.university.ecs.lab.common.utils.SourceToObjectUtils.parseClass;

/**
 * Service for extracting the differences between a local and remote repository and generating delta
 */
public class DeltaExtractionService {

  /** Config file, defaults to config.json */
  private final Config config;

  /** Config file, defaults to config.json */
  private final GitService gitService;


  /**
   * Constructor for the delta extraction service
   *
   * @param configPath file path to the configuration file
   */
  public DeltaExtractionService(String configPath) {
    this.gitService = new GitService(configPath);
    this.config = ConfigUtil.readConfig(configPath);
  }

  /**
   * Generate delta between base branch and base branch + 1
   */
  public void generateDelta() {
    List<DiffEntry> differences = null;

    try{
      differences = gitService.getDifferences(1);

    } catch (Exception e) {
      Error.reportAndExit(Error.GIT_FAILED);
    }

    // process/write differences to delta output
    String outputFile = processDifferences(differences);

    // Advance the local commit for next delta generation
    if(!gitService.resetLocal(1)) {
      System.out.println("No additional commits to fast forward to!");
    }


    return outputNames;
  }

  /**
   * Process the differences between the local and remote repository and write the differences to a
   * file. Differences can be generated from {@link GitFetchUtils#fetchRemoteDifferences(Repository,
   * String)}
   *
   * @param diffEntries the list of differences extracted
   * @param inputRepo the input repo to handle
   * @return the name of the output file generated
   * @throws IOException if a failure occurs while trying to write to the file
   */
  public String processDifferences(List<DiffEntry> diffEntries) {

    // Filter entries
    List<DiffEntry> filteredEntries = filterDiffEntries(diffEntries);

    SystemChange systemChange = new SystemChange();

    // process each difference
    for (DiffEntry entry : filteredEntries) {
      String basePath = config.getLocalPath(inputRepo) + "/";
      System.out.println("Extracting changes from: " + basePath);

      boolean isDeleted = DiffEntry.ChangeType.DELETE.equals(entry.getChangeType());
      String localPath = isDeleted ? basePath + entry.getOldPath() : basePath + entry.getNewPath();
      File classFile = new File(localPath);

      try {
        JClass jClass = parseClass(classFile, config);
        if (jClass != null) {
          systemChange.addChange(jClass, entry, localPath);
        }
      } catch (IOException e) {
        System.err.println("Error parsing class file: " + classFile.getAbsolutePath());
        System.err.println(e.getMessage());
        System.exit(DELTA_EXTRACTION_FAIL.ordinal());
      }

      System.out.println(
          "Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());
    }

    String outputName = FullCimetUtils.getDeltaOutputName(branch, compareCommit);

    MsJsonWriter.writeJsonToFile(systemChange.toJsonObject(), outputName);

    System.out.println("Delta extracted: " + outputName);

    return outputName;
  }

  /**
   * Returns only Java related files and accounts for discrepancy when
   * dealing with deleted files whose "new path" does not exist.
   *
   * @param diffEntries the diff entry list to filter
   * @return filtered list of diff entries
   */
  private List<DiffEntry> filterDiffEntries(List<DiffEntry> diffEntries) {
    return diffEntries.stream()
            .filter(
                    diffEntry -> {
                      if (DiffEntry.ChangeType.DELETE.equals(diffEntry.getChangeType())) {
                        return diffEntry.getOldPath().endsWith(".java");
                      } else {
                        return diffEntry.getNewPath().endsWith(".java");
                      }
                    })
            .collect(Collectors.toUnmodifiableList());
  }

}
