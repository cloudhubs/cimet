package edu.university.ecs.lab.delta.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.MicroserviceSystem;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import edu.university.ecs.lab.delta.models.Delta;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.models.enums.ChangeType;
import edu.university.ecs.lab.delta.models.enums.FileType;
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
    gitService.resetLocal(commitNew);

    // process/write differences to delta output
    generateDelta(differences);

  }

  /**
   * Process the differences between the local and remote repository and write the differences to a
   * file.
   *
   * @param diffEntries the list of differences extracted
   */
  public void generateDelta(List<DiffEntry> diffEntries) {

    // Set up a new SystemChangeObject
    SystemChange systemChange = new SystemChange();
    systemChange.setOldCommit(commitOld);
    systemChange.setNewCommit(commitNew);

    // process each difference
    for (DiffEntry entry : diffEntries) {

      String oldPath;
      String newPath;
      if(DiffEntry.ChangeType.DELETE.equals(entry.getChangeType())) {
        oldPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getOldPath().replace("/", File.separator);
        newPath = null;

      } else if(DiffEntry.ChangeType.ADD.equals(entry.getChangeType())) {
        oldPath = null;
        newPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getNewPath().replace("/", File.separator);

      } else {
        oldPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getOldPath().replace("/", File.separator);
        newPath = FileUtils.getClonePath(config.getRepoName()) + File.separator + entry.getNewPath().replace("/", File.separator);

      }

      String microserviceName = null;

      // Find the JClass associated with the change, null under certain cases
      File file = newPath == null ? new File(oldPath) : new File(newPath);

      JClass jClass = null;
      if(file.isFile() && file.getPath().endsWith(".java")) {
        jClass = getClass(entry, newPath);
        microserviceName = getMicroserviceName(entry);
      }

      // If the class isn't ours and it isn't a folder or Docker or Pom
      if(Objects.isNull(jClass) && !file.isFile() && !file.getName().equals("DockerFile") && !file.getName().equals("pom.xml")) {
        continue;
      }

      systemChange.getChanges().add(createDelta(oldPath, newPath, entry, jClass, microserviceName));

    }

    JsonReadWriteUtils.writeToJSON("./output/Delta.json", systemChange);

    System.out.println("Delta extracted: from " + commitOld + " to " + commitNew + " at ./output/Delta.json");

  }

  /**
   * Returns only Java related files and accounts for discrepancy when
   * dealing with deleted files whose "new path" does not exist.
   *
   * @param diffEntries the diff entry list to filter
   * @return filtered list of diff entries
   */
//  private List<DiffEntry> filterDiffEntries(List<DiffEntry> diffEntries) {
//    return diffEntries.stream()
//            .filter(
//                    diffEntry -> {
//                      if (DiffEntry.ChangeType.DELETE.equals(diffEntry.getChangeType())) {
//                        return diffEntry.getOldPath().endsWith(".java");
//                      } else {
//                        return diffEntry.getNewPath().endsWith(".java");
//                      }
//                    })
//            .collect(Collectors.toUnmodifiableList());
//  }

  private JClass getClass(DiffEntry diffEntry, String localPath) {
    // If the DiffEntry type is DELETE then we cannot pare at this HEAD, it is now gone
    if(DiffEntry.ChangeType.DELETE.equals(diffEntry.getChangeType())) {
      return null;
    } else {
      // Otherwise we can parse the new file as we have reset the head to where the file exists
      return SourceToObjectUtils.parseClass(new File(localPath), config);


    }
  }

  // TODO Add to FileUtils
  private String getLocalPath(DiffEntry diffEntry) {
    if(DiffEntry.ChangeType.DELETE.equals(diffEntry.getChangeType())) {
      return FileUtils.getClonePath(config.getRepoName()) + File.separator + diffEntry.getOldPath().replace("/", File.separator);
    } else {
      return FileUtils.getClonePath(config.getRepoName()) + File.separator + diffEntry.getNewPath().replace("/", File.separator);
    }
  }

  // TODO Add to FileUtils
  private String getMicroserviceName(DiffEntry diffEntry) {
    if(DiffEntry.ChangeType.DELETE.equals(diffEntry.getChangeType())) {
      return diffEntry.getOldPath().substring(0, diffEntry.getOldPath().indexOf("/"));
    } else {
      return diffEntry.getNewPath().substring(0, diffEntry.getNewPath().indexOf("/"));
    }
  }

  private Delta createDelta(String oldPath, String newPath, DiffEntry entry, JClass jClass, String microserviceName) {
    FileType fileType = FileType.DIRECTORY;
    String path = newPath == null ? oldPath : newPath;
    if(new File(path).isFile()) {
      fileType = FileType.FILE;
    }

    return new Delta(oldPath, newPath, ChangeType.fromDiffEntry(entry), fileType, jClass, microserviceName);
  }

}
