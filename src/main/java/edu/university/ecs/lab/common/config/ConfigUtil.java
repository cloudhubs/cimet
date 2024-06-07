package edu.university.ecs.lab.common.config;

import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;

import java.io.File;

import static edu.university.ecs.lab.common.error.Error.UNKNOWN_ERROR;

/** Utility class for reading and validating the input config file */
public class ConfigUtil {

  /** Prevent instantiation */
  private ConfigUtil() {}

  /**
   * This method read's the input config and return Config object
   *
   * @param configPath path to the input config file
   * @return Config object
   */
  public static Config readConfig(String configPath) {
    return JsonReadWriteUtils.readFromJSON(configPath, Config.class);
  }

//  public static String getGitRelativePath(String path) {
//    if(!path.startsWith("." + File.separator + DEFAULT_CLONE_PATH)) {
//      Error.reportAndExit(UNKNOWN_ERROR);
//    }
//
//    return path.replace("." + File.separator + DEFAULT_CLONE_PATH, "");
//  }

//  public static String getMicroserviceNameFromPath(String path) {
//    if(!path.startsWith("." + File.separator + DEFAULT_CLONE_PATH + File.separator)) {
//      Error.reportAndExit(UNKNOWN_ERROR);
//    }
//
//    return path.replace("." + File.separator + DEFAULT_CLONE_PATH + File.separator, "").split("\\\\")[0];
//  }

//  public static String getBaseOutputPath() {
//    return "./" + DEFAULT_OUTPUT_PATH;
//  }
//
//  public static String getBaseClonePath() {
//    return "./" + DEFAULT_CLONE_PATH;
//  }



}
