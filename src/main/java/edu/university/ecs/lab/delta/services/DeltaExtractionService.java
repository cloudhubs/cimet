package edu.university.ecs.lab.delta.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import edu.university.ecs.lab.delta.models.SystemChange;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for extracting the differences between a local and remote repository and generating delta
 */
public class DeltaExtractionService {

  /** Config file, defaults to config.json */
  private final Config config;

  /** Config file, defaults to config.json */
  private final GitService gitService;

  private final String commitOld;

  private final String commitNew;


  /**
   * Constructor for the delta extraction service
   *
   * @param configPath file path to the configuration file
   */
  public DeltaExtractionService(String configPath, String commitOld, String commitNew) {
    this.gitService = new GitService(configPath);
    this.config = ConfigUtil.readConfig(configPath);
    this.commitOld = commitOld;
    this.commitNew = commitNew;
  }

  /**
   * Generate delta between base branch and base branch + 1
   */
  public void generateDelta() {
    List<DiffEntry> differences = null;

    // Ensure we start at commitOld
    gitService.resetLocal(commitOld);

    try{
      differences = gitService.getDifferences(commitOld, commitNew);

    } catch (Exception e) {
      Error.reportAndExit(Error.GIT_FAILED);
    }

    // Advance the local commit for parsing
    if(!gitService.resetLocal(1)) {
      System.out.println("No additional commits to fast forward to!");
    }

    // process/write differences to delta output
    processDifferences(differences);

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
    systemChange.setOldCommit(commitOld);
    systemChange.setNewCommit(commitNew);

    // process each difference
    for (DiffEntry entry : filteredEntries) {
      String basePath = FileUtils.getClonePath(config.getRepoName()) + File.separator;
      System.out.println("Extracting changes from: " + basePath);

      boolean isDeleted = DiffEntry.ChangeType.DELETE.equals(entry.getChangeType());
      String localPath;
      String microserviceName = "";

      if(isDeleted) {
          localPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getOldPath().replace("/", File.separator);
          microserviceName = entry.getOldPath().substring(0, entry.getOldPath().indexOf("/"));
      } else {
          localPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getNewPath().replace("/", File.separator);
          microserviceName = entry.getNewPath().substring(0, entry.getNewPath().indexOf("/"));

      }

      File classFile = new File(localPath);

      try {
        JClass jClass = SourceToObjectUtils.parseClass(classFile, config);
        if (jClass != null) {
          systemChange.addDelta(jClass, entry, localPath, microserviceName);
        }
      } catch (IOException e) {
        Error.reportAndExit(Error.JPARSE_FAILED);
      }

      System.out.println("Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());
    }

    JsonReadWriteUtils.writeToJSON("./output/Delta.json", systemChange);

    System.out.println("Delta extracted: " + "./output/Delta.json");

    return "./output/Delta.json";
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
