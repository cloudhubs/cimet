package edu.university.ecs.lab.common.utils;

import com.google.gson.*;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.models.enums.FileType;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import org.json.JSONObject;
import org.json.XML;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
     */
    public static ConfigFile readFromYaml(String path, Config config) {
        JsonObject data = null;
        Yaml yaml = new Yaml(new SafeConstructor());
        Gson gson = new Gson();

        try (FileInputStream fis = new FileInputStream(path)) {
            // Parse YAML file to Map
            Map<String, Object> yamlMap = yaml.load(fis);

            if (yamlMap == null || yamlMap.isEmpty()) {
                // Handle empty file or empty YAML content
                data = new JsonObject();
            } else {
                String jsonString = gson.toJson(yamlMap);
                data =  JsonParser.parseString(jsonString).getAsJsonObject();
            }

        } catch (Exception e) {
            // Handle I/O errors (file not found, etc.)
            return null;
        }

        return new ConfigFile(FileUtils.localPathToGitPath(path, config.getRepoName()), new File(path).getName(), data, FileType.CONFIG);
    }

    public static ConfigFile readFromDocker(String path, Config config) {
        List<String> instructions = new ArrayList<>();
        JsonObject jsonObject;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                instructions.add(line.trim());  // Add each line as an instruction
            }
        } catch (Exception e) {
            return null;
        }
        jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (String instruction : instructions) {
            jsonArray.add(instruction);
        }
        jsonObject.add("instructions", jsonArray);

        return new ConfigFile(FileUtils.localPathToGitPath(path, config.getRepoName()), new File(path).getName(), jsonObject, FileType.CONFIG);
    }

    public static ConfigFile readFromPom(String path, Config config) {
        String xmlContent = null;
        JsonObject jsonObject;
        try {
            // Read the entire file content
            xmlContent = new String(Files.readAllBytes(Paths.get(path)));

            if (xmlContent.trim().isEmpty()) {
                jsonObject = new JsonObject();
            } else {
                // Convert XML to JSONObject using org.json
                JSONObject jsonObjectOld = XML.toJSONObject(xmlContent);

                // Convert JSONObject to Gson JsonObject
                JsonElement jsonElement = JsonParser.parseString(jsonObjectOld.toString());
                jsonObject = jsonElement.getAsJsonObject();
            }
        } catch (Exception e) {
            return null;
        }


        return new ConfigFile(FileUtils.localPathToGitPath(path, config.getRepoName()), new File(path).getName(), jsonObject, FileType.CONFIG);
    }

    public static ConfigFile readFromGradle(String path, Config config) {
        JsonObject jsonObject = new JsonObject();
        Stack<JsonObject> jsonStack = new Stack<>();
        jsonStack.push(jsonObject);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            String currentKey = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.endsWith("{")) {
                    String key = line.substring(0, line.length() - 1).trim();
                    JsonObject newObject = new JsonObject();
                    jsonStack.peek().add(key, newObject);
                    jsonStack.push(newObject);
                    currentKey = key;
                } else if (line.equals("}")) {
                    jsonStack.pop();
                    currentKey = null;
                } else if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim().replace("'", "\"");
                        jsonStack.peek().addProperty(key, value);
                    }
                } else {
                    if (currentKey != null) {
                        JsonArray array = jsonStack.peek().has(currentKey) ?
                                jsonStack.peek().getAsJsonArray(currentKey) : new JsonArray();
                        array.add(line);
                        jsonStack.peek().add(currentKey, array);
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }

        return new ConfigFile(
            FileUtils.localPathToGitPath(path, config.getRepoName()), 
            new File(path).getName(), 
            jsonObject, 
            FileType.CONFIG
        );
    }
    
}
