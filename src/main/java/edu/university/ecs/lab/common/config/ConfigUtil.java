package edu.university.ecs.lab.common.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.university.ecs.lab.common.error.Error;

import java.io.File;
import java.io.FileReader;

import static edu.university.ecs.lab.common.config.Config.*;
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
    JsonReader jsonReader = null;
    try {
      jsonReader = new JsonReader(new FileReader(configPath));
    } catch (Exception e) {
      Error.reportAndExit(Error.INVALID_CONFIG_PATH);
    }

    Gson gson = new Gson();

    return gson.fromJson(jsonReader, Config.class);
  }

  public static String getGitRelativePath(String path) {
    if(!path.startsWith("." + File.separator + DEFAULT_CLONE_PATH)) {
      Error.reportAndExit(UNKNOWN_ERROR);
    }

    return path.replace("." + File.separator + DEFAULT_CLONE_PATH, "");
  }

  public static String getMicroserviceNameFromPath(String path) {
    if(!path.startsWith("." + File.separator + DEFAULT_CLONE_PATH + File.separator)) {
      Error.reportAndExit(UNKNOWN_ERROR);
    }

    return path.replace("." + File.separator + DEFAULT_CLONE_PATH + File.separator, "").split("\\\\")[0];
  }

  public static String getBaseOutputPath() {
    return "./" + DEFAULT_OUTPUT_PATH;
  }

  public static String getBaseClonePath() {
    return "./" + DEFAULT_CLONE_PATH;
  }

  public static void createPaths() {
    try {
      new File(getBaseOutputPath()).mkdirs();
      new File(getBaseClonePath()).mkdirs();
    } catch (Exception e) {
      Error.reportAndExit(Error.UNKNOWN_ERROR);
    }
  }

}
