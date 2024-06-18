// package edu.university.ecs.lab.metrics.services;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.stream.Stream;

// import edu.university.ecs.lab.common.models.Microservice;
// import edu.university.ecs.lab.common.models.MicroserviceSystem;
// import edu.university.ecs.lab.metrics.models.metrics.TimeoutSettings;

// import java.util.Set;

// public class Timeout {

//     /**
//      * Method to analyze timeout settings for potential anti-patterns
//      * 
//      * @param microserviceSystem the microservice system to analyze
//      */
//     public void analyzeTimeoutSettings(MicroserviceSystem microserviceSystem) {
//         Set<Microservice> microservices = microserviceSystem.getMicroservices();
//         for (Microservice microservice : microservices) {
//             TimeoutSettings timeoutSettings = extractTimeoutSettings(microservice);

//             // Check for lack of timeout configuration
//             if (timeoutSettings.getTimeoutDuration() == null) {
//                 System.out.println("Microservice " + microservice.getName() + " has no timeout configuration.");
//             }
//         }
//     }

//     /**
//      * Method to extract timeout settings from the configuration file or annotations of a microservice
//      * 
//      * @param microservice the microservice to extract settings from
//      * @return extracted timeout settings
//      */
//     private TimeoutSettings extractTimeoutSettings(Microservice microservice) {
//         TimeoutSettings timeoutSettings = new TimeoutSettings();

//         String configFilePath = microservice.getPath() + "/src/main/resources/application.yml";

//         try (Stream<String> lines = Files.lines(Paths.get(configFilePath))) {
//             lines.forEach(line -> {
//                 if (line.contains("resilience4j.timeout")) {
//                     // Parse the timeout settings, e.g., timeout.duration=1000ms
//                     if (line.contains("timeout.duration")) {
//                         String timeoutValue = line.split(":")[1].trim();
//                         timeoutSettings.setTimeoutDuration(timeoutValue);
//                     }
//                 }
//             });
//         } catch (IOException e) {
//             e.printStackTrace();
//         }

//         return timeoutSettings;
//     }

// }
