package edu.university.ecs.lab.common.services;


import edu.university.ecs.lab.common.error.Error;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Static functions to manage logger object
 */
public class LoggerManager {
    private static final Logger logger = LogManager.getLogger(LoggerManager.class);


    /**
     * Log an info message
     * 
     * @param msgSupplier the message to log
     */
    public static void info(Supplier<String> msgSupplier) {
        log(Level.INFO, msgSupplier);
    }

    /**
     * Log a warning message
     * 
     * @param msgSupplier the message to log
     */
    public static void warn(Supplier<String> msgSupplier) {
        log(Level.WARN, msgSupplier);
    }

    /**
     * Log a debug message
     * 
     * @param msgSupplier the message to log
     */
    public static void debug(Supplier<String> msgSupplier) {
        log(Level.DEBUG, msgSupplier);
    }

    /**
     * Log an error message
     * 
     * @param msgSupplier the message to log
     */
    public static void error(Supplier<String> msgSupplier, Optional<Exception> exception) {
        log(Level.ERROR, msgSupplier);
        exception.ifPresent(e -> logger.error(e.getMessage(), e));
    }

    /**
     * Log message
     * 
     * @param level the logging level
     * @param msgSupplier the message to log
     */
    private static void log(Level level, Supplier<String> msgSupplier) {
        logger.log(level, msgSupplier.get());
    }


}
