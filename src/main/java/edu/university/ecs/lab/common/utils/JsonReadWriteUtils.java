package edu.university.ecs.lab.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.ir.Method;
import edu.university.ecs.lab.common.models.ir.MethodCall;
import edu.university.ecs.lab.common.models.ir.ProjectFile;
import edu.university.ecs.lab.common.models.serialization.MethodCallDeserializer;
import edu.university.ecs.lab.common.models.serialization.MethodDeserializer;
import edu.university.ecs.lab.common.models.serialization.ProjectFileDeserializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Utility class for reading and writing JSON to a file.
 */
public class JsonReadWriteUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private JsonReadWriteUtils() {
    }

    /**
     * Writes an object to a JSON file at a specified path.
     *
     * @param <T>      the type of the object to write
     * @param object   the object to serialize into JSON
     * @param filePath the file path where the JSON should be saved
     */
    public static <T> void writeToJSON(String filePath, T object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(object, writer);
            }
        } catch (IOException e) {
            Error.reportAndExit(Error.INVALID_JSON_WRITE, Optional.of(e));
        }
    }

    /**
     * Reads a JSON file from a given path and converts it into an object of the specified type.
     *
     * @param <T>      the type of the object to return
     * @param filePath the file path to the JSON file
     * @param type     the Class representing the type of the object to deserialize
     * @return an object of type T containing the data from the JSON file
     */
    public static <T> T readFromJSON(String filePath, Class<T> type) {
        // Register appropriate deserializers to allow compaction of data

        Gson gson = registerDeserializers();
        try (Reader reader = new BufferedReader(new FileReader(filePath))) {
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            Error.reportAndExit(Error.INVALID_JSON_READ, Optional.of(e));
        }

        return null;
    }

    /**
     * Function for register custom deserializers when reading JSON from a file
     * @return
     */
    public static Gson registerDeserializers() {

        return new GsonBuilder()
                .registerTypeAdapter(Method.class, new MethodDeserializer())
                .registerTypeAdapter(MethodCall.class, new MethodCallDeserializer())
                .registerTypeAdapter(ProjectFile.class, new ProjectFileDeserializer())
                .create();
    }
}
