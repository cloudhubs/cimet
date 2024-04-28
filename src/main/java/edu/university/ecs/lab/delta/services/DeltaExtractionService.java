package edu.university.ecs.lab.delta.services;

import edu.university.ecs.lab.common.config.models.InputConfig;
import edu.university.ecs.lab.common.config.models.InputRepository;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.utils.JsonToObjectUtils;
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

/**
 * Service for extracting the differences between a local and remote repository and generating delta
 */
public class DeltaExtractionService {

  /** The branch to compare to */
  private final String branch;

  /** Config file, defaults to config.json */
  private final InputConfig config;

  /**
   * Constructor for the delta extraction service.
   *
   * @param branch the branch to compare to
   * @param config input configuration file
   */
  public DeltaExtractionService(String branch, InputConfig config) {
    this.branch = branch;
    this.config = config;
  }

  /**
   * Top level generate the delta between the local and remote repository.
   * @return set of output file names generated
   */
  public Set<String> generateDelta() {
    Set<String> outputNames = new HashSet<>();

    // iterate through each repository path
    for (InputRepository inputRepository : config.getRepositories()) {
        try (Repository localRepo = GitFetchUtils.establishLocalEndpoint(config.getLocalPath(inputRepository))) {
            // point to local repository

            // extract remote differences with local
            List<DiffEntry> differences = GitFetchUtils.fetchRemoteDifferences(localRepo, branch);

            // process/write differences to delta output
            String outputFile = this.processDifferences(differences, inputRepository);
            outputNames.add(outputFile);
        } catch (Exception e) {
            System.err.println("Error extracting delta: " + e.getMessage());
            System.exit(DELTA_EXTRACTION_FAIL.ordinal());
        }
    }
    return outputNames;
  }

  /**
   * Process the differences between the local and remote repository and write the differences to a
   * file. Differences can be generated from {@link GitFetchUtils#fetchRemoteDifferences(Repository, String)}
   *
   * @param diffEntries the list of differences extracted
   * @param inputRepo   the input repo to handle
   * @return the name of the output file generated
   * @throws IOException  if a failure occurs while trying to write to the file
   */
  public String processDifferences(List<DiffEntry> diffEntries, InputRepository inputRepo) throws IOException {

    // Set local repo to latest commit
    advanceLocalRepo(inputRepo);

    // All java files
    List<DiffEntry> filteredEntries = diffEntries.stream()
            .filter(diffEntry ->
                    {
                      if (DiffEntry.ChangeType.DELETE.equals(diffEntry.getChangeType())) {
                        return diffEntry.getOldPath().endsWith(".java");
                      } else {
                        return diffEntry.getNewPath().endsWith(".java");
                      }
                    })
            .collect(Collectors.toUnmodifiableList());

    SystemChange systemChange = new SystemChange();

    // process each difference
    for (DiffEntry entry : filteredEntries) {
      String basePath = config.getLocalPath(inputRepo) + "/";
      System.out.println("Extracting changes from: " + basePath);

      boolean isDeleted = DiffEntry.ChangeType.DELETE.equals(entry.getChangeType());
      String localPath = isDeleted ? basePath + entry.getOldPath() : basePath + entry.getNewPath();
      File classFile = new File(localPath);

      JClass jClass = null;
      try {
        if (!Objects.equals(DiffEntry.ChangeType.DELETE, entry.getChangeType())) {
          jClass = JsonToObjectUtils.parseClass(classFile, config);
        } else {
          // TODO delete logic and remove the continue;
          System.out.println("Deleted file detected, not yet implemented: " + classFile.getAbsolutePath());
          continue;
        }
      } catch (IOException e) {
        System.err.println("Error parsing class file: " + classFile.getAbsolutePath());
        System.err.println(e.getMessage());
        System.exit(DELTA_EXTRACTION_FAIL.ordinal());
      }

      systemChange.addChange(jClass, entry, localPath);

      System.out.println(
          "Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());
    }


    String outputName = "./out/delta-changes-[" + (new Date()).getTime() + "].json";

    MsJsonWriter.writeJsonToFile(systemChange.toJsonObject(), outputName);

    System.out.println("Delta extracted: " + outputName);

    return outputName;
  }

  /**
   * Advance the local repository to the latest commit on the remote branch.
   *
   * @param inputRepository the input repository to advance
   */
  private void advanceLocalRepo(InputRepository inputRepository) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("git", "reset", "--hard", "origin/main");
      processBuilder.directory(new File(Path.of(config.getLocalPath(inputRepository)).toAbsolutePath().toString()));
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      int exitCode = process.waitFor();
    } catch (IOException | InterruptedException e) {
      System.err.println("Error advancing local repository: " + e.getMessage());
      System.exit(DELTA_EXTRACTION_FAIL.ordinal());
    }
  }
}
