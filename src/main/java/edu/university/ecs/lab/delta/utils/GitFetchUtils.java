//package edu.university.ecs.lab.delta.utils;
//
//import edu.university.ecs.lab.common.error.Error;
//import org.eclipse.jgit.api.FetchCommand;
//import org.eclipse.jgit.api.DiffCommand;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.diff.DiffEntry;
//import org.eclipse.jgit.lib.*;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.eclipse.jgit.revwalk.RevTree;
//import org.eclipse.jgit.revwalk.RevWalk;
//import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
//import org.eclipse.jgit.treewalk.AbstractTreeIterator;
//import org.eclipse.jgit.treewalk.CanonicalTreeParser;
//import org.eclipse.jgit.treewalk.FileTreeIterator;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
///** Utility class for fetching differences between local and remote git repositories. */
//public class GitFetchUtils {
//  /** Private constructor to prevent instantiation */
//  private GitFetchUtils() {}
//
////  /**
////   * Establish a local endpoint for the given repository path.
////   *
////   * @param path the path to the repository
////   * @return the repository object
////   */
////  public static Repository establishLocalEndpoint(String path) {
////    Repository repository = null;
////
////    try {
////      File localRepoDir = new File(path);
////      repository = new FileRepositoryBuilder().setGitDir(new File(localRepoDir, ".git")).build();
////
////    } catch (Exception e) {
////      Error.reportAndExit(Error.GIT_FAILED);
////    }
////
////    return repository;
////  }
//
////  /**
////   * Fetch the differences between the local repository and the remote repository.
////   *
////   * @param repo the repository object established by {@link #establishLocalEndpoint(String)}
////   * @param commit the commit to compare to the local repository
////   * @return the list of differences
////   * @throws Exception as generated from {@link FetchCommand#call()} or {@link DiffCommand#call()}
////   */
////  public static List<DiffEntry> fetchRemoteDifferences(Repository repo, String commit)
////      throws Exception {
////    try (Git git = new Git(repo)) {
////      // fetch latest changes from remote
////      git.fetch().call();
////
////      try (ObjectReader reader = repo.newObjectReader()) {
////        // get the difference between local main and origin/main
////        return git.diff()
////            .setOldTree(prepareLocalTreeParser(repo))
////            .setNewTree(prepareRemoteTreeParser(reader, repo, commit))
////            .call();
////      }
////    }
////  }
////
////  /**
////   * Prepare the tree parser for the given repository and git branch reference.
////   *
////   * @param reader the jgit reader
////   * @param repo the jgit repository object
////   * @param sourceCommit the commit hash of the source branch
////   * @return the canonical tree parser
////   * @throws IOException if an I/O error occurs from parsing the tree
////   */
////  private static CanonicalTreeParser prepareRemoteTreeParser(
////      ObjectReader reader, Repository repo, String sourceCommit) throws IOException {
////    try (RevWalk walk = new RevWalk(reader)) {
////      ObjectId commitObj = repo.resolve(sourceCommit);
////      RevCommit commit = repo.parseCommit(commitObj);
////      RevTree tree = walk.parseTree(commit.getTree().getId());
////
////      CanonicalTreeParser treeParser = new CanonicalTreeParser();
////      try (ObjectReader newReader = walk.getObjectReader()) {
////        treeParser.reset(newReader, tree.getId());
////      }
////
////      walk.dispose();
////      return treeParser;
////    }
////  }
////
////  private static AbstractTreeIterator prepareLocalTreeParser(Repository repo) {
////    return new FileTreeIterator(repo);
////  }
//}
