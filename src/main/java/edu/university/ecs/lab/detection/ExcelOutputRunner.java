package edu.university.ecs.lab.detection;

import com.google.gson.JsonArray;
import edu.university.ecs.lab.common.config.Config;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.sdg.MethodDependencyGraph;
import edu.university.ecs.lab.common.models.sdg.ServiceDependencyGraph;
import edu.university.ecs.lab.common.services.GitService;
import edu.university.ecs.lab.common.utils.FileUtils;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.delta.models.SystemChange;
import edu.university.ecs.lab.delta.services.DeltaExtractionService;
import edu.university.ecs.lab.detection.antipatterns.services.*;
import edu.university.ecs.lab.detection.architecture.models.*;
import edu.university.ecs.lab.detection.architecture.services.ARDetectionService;
import edu.university.ecs.lab.detection.metrics.RunCohesionMetrics;
import edu.university.ecs.lab.detection.metrics.models.ConnectedComponentsModularity;
import edu.university.ecs.lab.detection.metrics.models.DegreeCoupling;
import edu.university.ecs.lab.detection.metrics.models.StructuralCoupling;
import edu.university.ecs.lab.detection.metrics.services.MetricResultCalculation;
import edu.university.ecs.lab.intermediate.create.services.IRExtractionService;
import edu.university.ecs.lab.intermediate.merge.services.MergeService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ExcelOutputRunner {


    public static void main(String[] args) throws IOException {
        DetectionService detectionService = new DetectionService("./config.json");
        detectionService.runDetection();
    }

}
