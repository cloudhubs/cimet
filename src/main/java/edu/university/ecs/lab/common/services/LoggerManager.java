package edu.university.ecs.lab.common.services;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LoggerManager {
    private static final String ERR_MSG = "An error has occurred: ";

    private static final Logger logger = LogManager.getLogger(LoggerManager.class);

    public static void log(Level level, String msg) {
        logger.log(level, msg);
    }

    public static void error(Exception e) {
        logger.error(ERR_MSG, e);
    }

    public static void changeOutputFile(String fileName) {
        // Obtain the current LoggerContext
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        Appender appender =
                config.getAppender("LogFile"); // Replace "MyFileAppender" with your appender's name

        if (appender instanceof FileAppender) {
            appender.stop(); // Stop the appender before making changes

            FileAppender fileAppender = (FileAppender) appender;
            FileAppender newFileAppender =
                    FileAppender.newBuilder()
                            .withFileName("logs/" + fileName) // Specify the new file path
                            .withAppend(true)
                            .withBufferedIo(true)
                            .withBufferSize(8192)
                            .setConfiguration(config)
                            .withLayout(fileAppender.getLayout())
                            .withLocking(false)
                            .withName(fileAppender.getName())
                            .withIgnoreExceptions(fileAppender.ignoreExceptions())
                            .withFilter(fileAppender.getFilter())
                            .build();

            // Replace the old appender with the new one in the configuration
            config.addAppender(newFileAppender);
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger())
                    .addAppender(newFileAppender);
            context.updateLoggers(); // Update the context to apply changes

            LoggerConfig loggerConfig =
                    config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); // For root logger
            loggerConfig.removeAppender("MyFileAppender"); // Remove the old appender
            loggerConfig.addAppender(newFileAppender, null, null); // Add the new one
            context.updateLoggers(); // Apply the changes
        }
    }
}
