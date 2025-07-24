// utils/DiagnosticLogger.java
package orgs.tuasl_clint.utils;

import java.io.BufferedWriter;
import java.io.File; // Import File
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiagnosticLogger {
    private static final String LOGS_FOLDER = "src/main/resources/orgs/tuasl_clint/logs/";
    // Map to hold log queues for different log files
    private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> logQueues = new ConcurrentHashMap<>();
    // Map to hold schedulers for different log files
    private static final ConcurrentHashMap<String, ScheduledExecutorService> schedulers = new ConcurrentHashMap<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // Default log file name for backward compatibility or general logs
    private static final String DEFAULT_LOG_FILE = "application_diagnostics.log";

    /**
     * Initializes the logger for a specific log file.
     * This should be called once per log file name at application startup or before first use.
     * @param logFileName The name of the log file (e.g., "chat_controller_diagnostics.log").
     */
    public static void initializeLogger(String logFileName) {
        logQueues.putIfAbsent(logFileName, new ConcurrentLinkedQueue<>());
        schedulers.computeIfAbsent(logFileName, k -> {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> flushLogsToFile(k), 0, 1, TimeUnit.SECONDS);
            return scheduler;
        });
    }
    /**
     * Logs a general message to a specific diagnostic log file.
     * @param logFileName The name of the log file.
     * @param message The message to log.
     */
    public static void log(String logFileName, String message) {
        // Ensure the logger for this file is initialized
        initializeLogger(logFileName);
        String logEntry = String.format("[%s] %s%n", DATE_FORMAT.format(new Date()), message);
        logQueues.get(logFileName).offer(logEntry); // Add to queue
    }

    /**
     * Logs a general message to the default diagnostic log file.
     * @param message The message to log.
     */
    public static void log(String message) {
        log(DEFAULT_LOG_FILE, message);
    }

    /**
     * Logs a message along with the duration of a task to a specific diagnostic log file.
     * @param logFileName The name of the log file.
     * @param message The message describing the task.
     * @param startTimeMillis The start time of the task in milliseconds (e.g., System.currentTimeMillis()).
     */
    public static void logDuration(String logFileName, String message, long startTimeMillis) {
        // Ensure the logger for this file is initialized
        initializeLogger(logFileName);
        long duration = System.currentTimeMillis() - startTimeMillis;
        String logEntry = String.format("[%s] %s (Duration: %d ms)%n", DATE_FORMAT.format(new Date()), message, duration);
        logQueues.get(logFileName).offer(logEntry); // Add to queue
    }

    /**
     * تسجيل تفاصيل العملية مع قياس المدة المستغرقة
     * @param logFileName اسم ملف السجل
     * @param operationName اسم العملية
     * @param operationDetails تفاصيل العملية
     * @param startTime وقت بدء العملية
     */
    public static void logOperationDetails(String logFileName,
                                           String operationName,
                                           String operationDetails,
                                           long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        String logEntry = String.format("[%s] %s | %s | Duration: %d ms",
                DATE_FORMAT.format(new Date()),
                operationName,
                operationDetails,
                duration);
//        logQueues.get(LOGS_FOLDER + logFileName).offer(logEntry);
    }

    /**
     * Logs a message along with the duration of a task to the default diagnostic log file.
     * @param message The message describing the task.
     * @param startTimeMillis The start time of the task in milliseconds (e.g., System.currentTimeMillis()).
     */
    public static void logDuration(String message, long startTimeMillis) {
        logDuration(DEFAULT_LOG_FILE, message, startTimeMillis);
    }

    private static void flushLogsToFile(String logFileName) {
        ConcurrentLinkedQueue<String> queue = logQueues.get(logFileName);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        File logFile = new File(logFileName);
        File parentDir = logFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            // Attempt to create parent directories if they don't exist
            if (!parentDir.mkdirs()) {
                System.err.println("Failed to create parent directories for log file: " + logFileName);
                return; // Cannot write if directories cannot be created
            }
        }

        try (FileWriter fw = new FileWriter(logFileName, true); // Use logFileName directly
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String entry;
            while ((entry = queue.poll()) != null) {
                out.print(entry);
            }
        } catch (IOException e) {
            // Log this error to System.err as logging system itself is failing
            System.err.println("Error writing to diagnostic log file '" + logFileName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shuts down all active loggers and flushes remaining logs.
     */
    public static void shutdownAll() {
        schedulers.forEach((logFileName, scheduler) -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("DiagnosticLogger scheduler for '" + logFileName + "' did not terminate in time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("DiagnosticLogger shutdown for '" + logFileName + "' interrupted.");
            }
            flushLogsToFile(logFileName); // Ensure all remaining logs are written
        });
        logQueues.clear();
        schedulers.clear();
    }

    /**
     * Shuts down a specific logger and flushes its remaining logs.
     * @param logFileName The name of the log file to shut down.
     */
    public static void shutdown(String logFileName) {
        ScheduledExecutorService scheduler = schedulers.remove(logFileName);
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("DiagnosticLogger scheduler for '" + logFileName + "' did not terminate in time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("DiagnosticLogger shutdown for '" + logFileName + "' interrupted.");
            }
            flushLogsToFile(logFileName);
            logQueues.remove(logFileName);
        }
    }
}
