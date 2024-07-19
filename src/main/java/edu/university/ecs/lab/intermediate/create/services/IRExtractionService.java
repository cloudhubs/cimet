package edu.university.ecs.lab.intermediate.create.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.ir.ConfigFile;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * Top-level service for extracting intermediate representation from remote repositories. Methods
 * are allowed to exit the program with an error code if an error occurs.
 */
public class IRExtractionService {
    /**
     * Service to handle cloning from git
     */
    private final GitService gitService;

    private final Config config;

    /**
     * @param configPath path to configuration file
     */
    public IRExtractionService(String configPath) {
        gitService = new GitService(configPath);
        config = ConfigUtil.readConfig(configPath);
    }

    public IRExtractionService(Config config) {
        this.config = config;
        gitService = new GitService(config);
    }

    /**
     * Intermediate extraction runner, generates IR from remote repository and writes to file.
     *
     */
    public void generateIR(String fileName) {
        // Clone remote repositories and scan through each cloned repo to extract endpoints
        Set<Microservice> microservices = cloneAndScanServices();

        if (microservices.isEmpty()) {
            System.out.println("No microservices found");
        }

        //  Write each service and endpoints to IR
        writeToFile(microservices, fileName);

    }

    /**
     * Clone remote repositories and scan through each local repo and extract endpoints/calls
     *
     * @return a map of services and their endpoints
     */
    public Set<Microservice> cloneAndScanServices() {
        Set<Microservice> microservices = new HashSet<>();

        // Clone the repository present in the configuration file
        gitService.cloneRemote();

        // Start scanning from the root directory
        List<String> rootDirectories = findRootDirectories(FileUtils.getClonePath(config.getRepoName()));

        // Scan each root directory for microservices
        for (String rootDirectory : rootDirectories) {
            Microservice microservice = recursivelyScanFiles(rootDirectory);
            if (microservice != null) {
                microservices.add(microservice);
            }
        }

        return microservices;
    }

    /**
     * Recursively search for directories containing a microservice (pom.xml file)
     *
     * @param directory the directory to start the search from
     * @return a list of directory paths containing pom.xml
     */
    private List<String> findRootDirectories(String directory) {
        List<String> rootDirectories = new ArrayList<>();
        File root = new File(directory);
        if (root.exists() && root.isDirectory()) {
            // Check if the current directory contains a Dockerfile
            File[] files = root.listFiles();
            boolean containsPom = false;
            boolean containsGradle = false;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equals("pom.xml")) {
                        try {

                            // Create a DocumentBuilder
                            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                            // Parse the XML file
                            Document document = builder.parse(file);

                            // Normalize the XML Structure
                            document.getDocumentElement().normalize();

                            // Get all elements with the specific tag name
                            NodeList nodeList = document.getElementsByTagName("modules");
                            // Check if the tag is present
                            if (nodeList.getLength() == 0) {
                                containsPom = true;
                                break;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Error parsing pom.xml");
                        }
                    }
                    else if(file.isFile() && file.getName().equals("build.gradle")) {
                        try {
                        // Read the build.gradle file content
                        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

                        // Check if the file does not contain the 'subprojects' keyword
                        if (!content.contains("subprojects")) {
                            containsGradle = true;
                            break;
                        }
                        } catch (IOException e) {
                            throw new RuntimeException("Error reading build.gradle", e);
                        }
                    }
                }
            }
            if (containsPom) {
                rootDirectories.add(root.getPath());
                return rootDirectories;
            } else if (containsGradle){
                rootDirectories.add(root.getPath());
                return rootDirectories;
            } else {
                // Recursively search for directories containing a Dockerfile
                for (File file : files) {
                    if (file.isDirectory()) {
                        rootDirectories.addAll(findRootDirectories(file.getPath()));
                    }
                }
            }
        }
        return rootDirectories;
    }


    /**
     * Write each service and endpoints to intermediate representation
     *
     * @param microservices a list of microservices extracted from repository
     */
    private void writeToFile(Set<Microservice> microservices, String fileName) {

        MicroserviceSystem microserviceSystem = new MicroserviceSystem(config.getSystemName(), config.getBaseCommit(), microservices, new HashSet<>());

        JsonReadWriteUtils.writeToJSON("./output/" + fileName, microserviceSystem.toJsonObject());

        System.out.println("Successfully wrote rest extraction to: \"" + fileName + "\"");
    }

    /**
     * Recursively scan the files in the given repository path and extract the endpoints and
     * dependencies for a single microservice.
     *
     * @return model of a single service containing the extracted endpoints and dependencies
     */
    public Microservice recursivelyScanFiles(String rootMicroservicePath) {
        // Validate path exists and is a directory
        File localDir = new File(rootMicroservicePath);
        if (!localDir.exists() || !localDir.isDirectory()) {
            Error.reportAndExit(Error.INVALID_REPO_PATHS);
        }


        Microservice model = new Microservice(FileUtils.getMicroserviceNameFromPath(rootMicroservicePath),
                FileUtils.localPathToGitPath(rootMicroservicePath, config.getRepoName()));
        scanDirectory(localDir, model);

        System.out.println("Done!");
        return model;
    }

    /**
     * Recursively scan the given directory for files and extract the endpoints and dependencies.
     *
     * @param directory the directory to scan
     */
    public void scanDirectory(
            File directory,
            Microservice microservice) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, microservice);
                } else if (FileUtils.isValidFile(file.getPath())) {

                    if(FileUtils.isConfigurationFile(file.getPath())) {
                        ConfigFile configFile = SourceToObjectUtils.parseConfigurationFile(file, config);
                        if(configFile != null) {
                            microservice.getFiles().add(configFile);
                        }

                    } else {
                        JClass jClass = SourceToObjectUtils.parseClass(file, config, microservice.getName());
                        if (jClass != null) {
                            microservice.addJClass(jClass);
                        }
                    }


                }
            }
        }
    }


}
