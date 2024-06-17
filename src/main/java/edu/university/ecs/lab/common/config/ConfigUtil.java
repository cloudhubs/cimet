package edu.university.ecs.lab.common.config;

import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;

/**
 * Utility class for reading and validating the input config file
 */
public class ConfigUtil {

    /**
     * Prevent instantiation
     */
    private ConfigUtil() {
    }

    /**
     * This method read's the input config and return Config object
     *
     * @param configPath path to the input config file
     * @return Config object
     */
    public static Config readConfig(String configPath) {
        return JsonReadWriteUtils.readFromJSON(configPath, Config.class);
    }


}
