package edu.university.ecs.lab.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.*;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading files that don't abide by JSON format
 */
public class NonJsonReadWriteUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private NonJsonReadWriteUtils() {
    }

    /**
     * This method reads YAML from a file returning structure as JsonObject
     * @param path the path to the YAML file.
     * @return JsonObject YAML file structure as json object
     * @throws IOException If there is an error reading the YAML file.
     */
    public static ConfigFile readFromYaml(String path, Config config) {
        JsonNode yamlNode = null;
        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            yamlNode = yamlMapper.readTree(new File(path));
        } catch (IOException e) {
            Error.reportAndExit(Error.UNKNOWN_ERROR);
        }
        String jsonString = yamlNode.toString();
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }


        return new ConfigFile(FileUtils.localPathToGitPath(path, config.getRepoName()), new File(path).getName(), jsonObject, FileType.CONFIG);
    }

    public static ConfigFile readFromDocker(String path, Config config) {
        List<String> instructions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                instructions.add(line.trim());  // Add each line as an instruction
            }
        } catch (Exception e) {
            return null;
        }

        // Create the JSON object
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (String instruction : instructions) {
            jsonArray.add(instruction);
        }
        jsonObject.add("instructions", jsonArray);

        return new ConfigFile(FileUtils.localPathToGitPath(path, config.getRepoName()), new File(path).getName(), jsonObject, FileType.CONFIG);
    }

    public static ConfigFile readFromPom(String path, Config config) {
        String xmlContent = null;
        try {
            // Read the entire file content
            xmlContent = new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            return null;
        }

        // Convert XML to JSONObject using org.json
        JSONObject jsonObjectOld = XML.toJSONObject(xmlContent);

        // Convert JSONObject to Gson JsonObject
        JsonElement jsonElement = JsonParser.parseString(jsonObjectOld.toString());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        return new ConfigFile(FileUtils.localPathToGitPath(path, config.getRepoName()), new File(path).getName(), jsonObject, FileType.POM);
    }
}
