package edu.university.ecs.lab.common.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.university.ecs.lab.common.error.Error;

import java.io.FileReader;

import static edu.university.ecs.lab.common.error.Error.INVALID_CONFIG_PATH;

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

  public static String getAbsoluteClonePath() {
    return Paths.
  }

  public static String getAbsoluteOutputPath() {

  }
}
