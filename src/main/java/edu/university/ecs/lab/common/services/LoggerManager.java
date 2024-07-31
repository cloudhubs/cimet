package edu.university.ecs.lab.common.services;


import edu.university.ecs.lab.common.error.Error;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;
import java.util.function.Supplier;

public class LoggerManager {
    private static final Logger logger = LogManager.getLogger(LoggerManager.class);

    public static void info(Supplier<String> msgSupplier) {
        log(Level.INFO, msgSupplier);
    }

    public static void warn(Supplier<String> msgSupplier) {
        log(Level.WARN, msgSupplier);
    }

    public static void debug(Supplier<String> msgSupplier) {
        log(Level.DEBUG, msgSupplier);
    }

    public static void error(Supplier<String> msgSupplier, Optional<Exception> exception) {
        log(Level.ERROR, msgSupplier);
        exception.ifPresent(e -> logger.error(e.getMessage(), e));
    }


    private static void log(Level level, Supplier<String> msgSupplier) {
        logger.log(level, msgSupplier.get());
    }


}
