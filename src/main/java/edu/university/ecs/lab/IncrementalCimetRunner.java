package edu.university.ecs.lab;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.utils.FullCimetUtils;
import edu.university.ecs.lab.delta.DeltaExtractionRunner;
import edu.university.ecs.lab.intermediate.create.IRExtraction;
import edu.university.ecs.lab.intermediate.merge.IRMergeRunner;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IncrementalCimetRunner {

  private static final Logger LOGGER = Logger.getLogger(IncrementalCimetRunner.class.getName());

  static {
    try {
      // This block configure the logger with handler and formatter
      FileHandler fh = new FileHandler("MyLogFile.log");
      LOGGER.addHandler(fh);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Main method for full incremental report
   *
   * @param args /path/to/config/file <base branch> <base commit> <compare commit>
   */
  public static void main(String[] args) throws IOException {

    if (args.length != 4) {
      System.err.println(
          "Required arguments /path/to/config/file <branch> <base commit> <compare commit>");
      return;
    }

    String configPath = args[0];
    String branch = args[1];
    String baseCommit = args[2];
    String compareCommit = args[3];

    // Run IR Extraction on the base commit
    LOGGER.info("Beginning IR Extraction: " + baseCommit);
    String[] IRArgs = {configPath, branch, baseCommit};
    try {
      IRExtraction.main(IRArgs);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Git Repository Path
    Config config = ConfigUtil.validateConfig(configPath);
    String repoPath = config.getClonePath() + File.separator + "train-ticket-microservices";

    try {

      // Get the Git Repository
      Repository repo = new RepositoryBuilder().setGitDir(new File(repoPath, ".git")).build();

      // Get the Git Object
      Git git = new Git(repo);

      // Fetch updates from the remote repository
      git.fetch().setRemote("origin").call();

      // Get all commits from the remote branch
      Iterable<RevCommit> commitsIterable =
          git.log().add(repo.resolve("refs/remotes/origin/" + branch)).call();

      // Convert Iterable to List
      List<RevCommit> commitsList =
          StreamSupport.stream(commitsIterable.spliterator(), false).collect(Collectors.toList());

      // Reverse the list
      Collections.reverse(commitsList);

      boolean isBaseCommit = false;
      boolean isDesiredCommit = false;

      for (int i = 0; i < commitsList.size(); i++) {

        // remove until base commit is found at beginning
        if (!isBaseCommit && commitsList.get(0).getName().equals(baseCommit)) {
          isBaseCommit = true;
          continue;
        } else if (!isBaseCommit) {
          commitsList.remove(0);
          i--;
          continue;
        }

        // remove until compare commit is found at end
        if (!isDesiredCommit
            && commitsList.get(commitsList.size() - 1).getName().equals(compareCommit)) {
          isDesiredCommit = true;
        } else if (!isDesiredCommit) {
          commitsList.remove(commitsList.size() - 1);
          i--;
        }
      }

      // Run Delta and Merge IR Extraction
      for (int i = 1; i < commitsList.size(); i++) {

        // Run Delta Extraction on next commit
        LOGGER.info("Beginning Delta Extraction: " + commitsList.get(i).getName());
        String[] deltaArgs = {branch, commitsList.get(i).getName(), configPath};
        DeltaExtractionRunner.main(deltaArgs);

        // If First merge use IR, else use new IR
        if (i == 1) {
          LOGGER.info("Beginning Merge IR Extraction: " + commitsList.get(i).getName());

          // Run IR Merge Runner on IR and Merge
          String[] mergeArgs = {
            FullCimetUtils.pathToIR,
            FullCimetUtils.pathToDelta,
            configPath,
            branch,
            commitsList.get(i).getName()
          };
          IRMergeRunner.main(mergeArgs);
        } else {
          LOGGER.info("Beginning Merge IR Extraction: " + commitsList.get(i).getName());

          // Run IR Merge Runner on IR and Merge
          String[] mergeArgs = {
            FullCimetUtils.pathToNewIR,
            FullCimetUtils.pathToDelta,
            configPath,
            branch,
            commitsList.get(i).getName()
          };
          IRMergeRunner.main(mergeArgs);
        }
      }

      System.out.println("Total commits: " + commitsList.size());
      System.out.println("Final IR representation: " + FullCimetUtils.pathToNewIR);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
