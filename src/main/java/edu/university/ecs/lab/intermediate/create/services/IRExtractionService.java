package edu.university.ecs.lab.intermediate.create.services;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.error.Error;
import edu.university.ecs.lab.common.models.ir.JClass;
import edu.university.ecs.lab.common.models.ir.Microservice;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.common.utils.SourceToObjectUtils;

import java.io.File;
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

    // TODO REMOVE FOR TESTING ONLY
    public IRExtractionService(Config config) {
        gitService = new GitService(config);
        this.config = config;
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

        // Scan through each endpoint to update rest call destinations
//    updateCallDestinations(msDataMap);

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
                }
            }
            if (containsPom) {
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
     * Get name of output file for the IR
     *
     * @return the output file name
     */
    private String getOutputFileName() {
        return FileUtils.getBaseOutputPath()
                + "/rest-extraction-output-["
                + config.getBaseBranch()
                + "-"
                + config.getBaseCommit().substring(0, 7)
                + "].json";
    }

    /**
     * Recursively scan the files in the given repository path and extract the endpoints and
     * dependencies for a single microservice.
     *
     * @return model of a single service containing the extracted endpoints and dependencies
     */
    public Microservice recursivelyScanFiles(String rootMicroservicePath) {
//        System.out.println("Scanning repository '" + rootMicroservicePath + "'...");

        // Validate path exists and is a directory
        File localDir = new File(rootMicroservicePath);
        if (!localDir.exists() || !localDir.isDirectory()) {
            Error.reportAndExit(Error.INVALID_REPO_PATHS);
        }

        Set<JClass> controllers = new HashSet<>();
        Set<JClass> services = new HashSet<>();
        Set<JClass> repositories = new HashSet<>();
        Set<JClass> entities = new HashSet<>();


        scanDirectory(localDir, controllers, services, repositories, entities);

        String id = FileUtils.getMicroserviceNameFromPath(rootMicroservicePath);

        Microservice model =
                new Microservice(id, FileUtils.localPathToGitPath(rootMicroservicePath, config.getRepoName()), controllers, services, repositories, entities);

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
            Set<JClass> controllers,
            Set<JClass> services,
            Set<JClass> repositories,
            Set<JClass> entities) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, controllers, services, repositories, entities);
                } else if (file.getName().endsWith(".java")) {
                    scanFile(file, controllers, services, repositories, entities);
                }
            }
        }
    }

    /**
     * Scan the given file for endpoints and calls to other services.
     *
     * @param file the file to scan
     */
    public void scanFile(
            File file,
            Set<JClass> controllers,
            Set<JClass> services,
            Set<JClass> repositories,
            Set<JClass> entities) {
        JClass jClass = SourceToObjectUtils.parseClass(file, config);

        if (jClass == null) {
            return;
        }

        //jClass.setClassPath(removeFirstTwoComponents(jClass.getClassPath()));
        // Switch through class roles and handle additional logic if needed
        switch (jClass.getClassRole()) {
            case CONTROLLER:
                controllers.add(jClass);
                break;
            case SERVICE:
                services.add(jClass);
                break;
            case REPOSITORY:
                repositories.add(jClass);
                break;
            case ENTITY:
                entities.add(jClass);
                break;
            default:
                break;
        }

    }
}
