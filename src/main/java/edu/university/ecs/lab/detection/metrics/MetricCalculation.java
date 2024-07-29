package edu.university.ecs.lab.detection.metrics;

import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.detection.metrics.models.ConnectedComponentsModularity;
import edu.university.ecs.lab.detection.metrics.models.DegreeCoupling;
import edu.university.ecs.lab.detection.metrics.models.StructuralCoupling;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;

public class MetricCalculation {

    public static void main(String[] args) {
        Config config = ConfigUtil.readConfig("./config.json");

        // Create IR of first commit
        createIRSystem(config, "IR.json");

        // Create Microservice System based on generated IR
        MicroserviceSystem currentSystem = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);

        ServiceDependencyGraph sdg = new ServiceDependencyGraph(currentSystem);

        // Structural coupling
        StructuralCoupling sc = new StructuralCoupling(sdg);
        System.out.printf("Maximum Structural coupling: %.2f%n", sc.getMaxSC());
        System.out.printf("Average Structural coupling: %.2f%n", sc.getAvgSC());
        System.out.printf("Standard Deviation of Structural coupling: %.2f%n", sc.getStdSC());

        // Degree coupling
        DegreeCoupling dc = new DegreeCoupling(sdg);
        System.out.printf("Maximum AIS: %d%n", dc.getMaxAIS());
        System.out.printf("Average AIS: %.2f%n", dc.getAvgAIS());
        System.out.printf("Standard Deviation of AIS: %.2f%n", dc.getStdAIS());

        System.out.printf("Maximum ADS: %d%n", dc.getMaxADS());
        System.out.printf("Average ADS: %.2f%n", dc.getADCS());
        System.out.printf("Standard Deviation of ADS: %.2f%n", dc.getStdADS());

        System.out.printf("Maximum ACS: %d%n", dc.getMaxACS());
        System.out.printf("Average ACS: %.2f%n", dc.getADCS());
        System.out.printf("Standard Deviation of ACS: %.2f%n", dc.getStdACS());

        System.out.printf("Service Coupling Factor: %.2f%n", dc.getSCF());

        System.out.printf("Service Interdependence in the System: %d%n", dc.getSIY());

        // Modularity

        ConnectedComponentsModularity m = new ConnectedComponentsModularity(sdg);
        System.out.printf("Amount of Strongly Connected Components: %d%n", m.getSCC().size());
        System.out.printf("Modularity of Strongly Connected Components: %.2f%n", m.getModularity());
    }
    private static void createIRSystem(Config config, String fileName) {
        // Create both directories needed
        FileUtils.makeDirs();

        // Initialize the irExtractionService
        IRExtractionService irExtractionService = new IRExtractionService(fileName);

        // Generate the Intermediate Representation
        irExtractionService.generateIR(fileName);
    }
}
