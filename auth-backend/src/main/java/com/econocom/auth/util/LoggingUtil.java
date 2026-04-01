package com.econocom.auth.util;

import org.slf4j.Logger;

/**
 * Utility class for standardized logging across the application.
 */
public final class LoggingUtil {

    private LoggingUtil() {
        // Utility class
    }


    public static final String MSG_LOG = "%s [%s] :: [%s] :: %s";   // Format: PROJECT [Class] :: [Method] :: Message
    public static final String MSG_LOG_PROJECT = "ECONOCOM_AUTH";

    public static final String TRACE = "trace";
    public static final String DEBUG = "debug";
    public static final String INFO = "info";
    public static final String WARN = "warn";
    public static final String ERROR = "error";


    /**
     * Logs a message with standardized format.
     *
     * @param logger the SLF4J logger
     * @param level the log level (trace, debug, info, warn, error)
     * @param className the class name
     * @param methodName the method name
     * @param message the message to log
     */
    public static void log(Logger logger, String level, String className, String methodName, String message) {

        if (logger == null) {
            return;
        }

        String safeLevel = null;

        if (level != null) {

            safeLevel = level;

        } else {
            safeLevel = INFO;
        }

        String formatted = String.format(MSG_LOG, MSG_LOG_PROJECT, className, methodName, message);

        switch (safeLevel.toLowerCase()) {
            
            case TRACE:
                logger.trace(formatted);
                break;

            case DEBUG:
                logger.debug(formatted);
                break;

            case INFO:
                logger.info(formatted);
                break;

            case WARN:
                logger.warn(formatted);
                break;

            case ERROR:
                logger.error(formatted);
                break;
                
            default:
                logger.info(formatted);
        }
    }
}
