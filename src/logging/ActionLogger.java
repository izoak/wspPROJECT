package logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for appending timestamped log entries to a persistent log file.
 *
 * <p>All public methods are {@code static}; this class cannot be instantiated.
 * Log entries are written to {@code logs/system.log}, relative to the working
 * directory. The directory is created automatically if it does not exist.
 *
 * <p>Each log entry has the format:
 * <pre>
 *   [yyyy-MM-dd HH:mm:ss] &lt;message&gt;
 * </pre>
 *
 * <p><strong>Usage:</strong>
 * <pre>{@code
 *   ActionLogger.log("User logged in: alice");
 *   List<String> allLogs = ActionLogger.readLogs();
 * }</pre>
 *
 * @author Gotei 4
 * @version 1.0
 */
public final class ActionLogger {

    /** Directory where log files are stored (relative to the process working directory). */
    private static final Path LOG_DIRECTORY = Path.of("logs");

    /** Path to the primary log file. */
    private static final Path LOG_FILE = LOG_DIRECTORY.resolve("system.log");

    /** Formatter used to produce human-readable timestamps in each log entry. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Private constructor — this class is not instantiable.
     * All functionality is exposed through static methods.
     */
    private ActionLogger() { }

    /**
     * Appends a timestamped log entry to {@code logs/system.log}.
     *
     * <p>If the log directory or file does not yet exist it is created on
     * the first call. Each invocation opens the file, appends one line, and
     * closes the file, so no explicit flush or shutdown is needed.
     *
     * <p>Example output line:
     * <pre>
     *   [2025-03-14 09:26:53] User logged in: alice
     * </pre>
     *
     * @param message the message to log (must not be {@code null})
     * @throws IllegalStateException if an I/O error occurs while writing
     *                               (wraps the underlying {@link IOException})
     */
    public static void log(String message) {
        try {
            Files.createDirectories(LOG_DIRECTORY);
            String line = "[" + LocalDateTime.now().format(FORMATTER) + "] "
                        + message + System.lineSeparator();
            Files.writeString(LOG_FILE, line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write log entry", e);
        }
    }

    /**
     * Reads and returns all lines from the log file.
     *
     * <p>Returns an empty list (not {@code null}) if the log file does not
     * yet exist (i.e. nothing has been logged yet).
     *
     * @return unmodifiable list of log lines in file order (oldest first)
     * @throws IllegalStateException if an I/O error occurs while reading
     *                               (wraps the underlying {@link IOException})
     */
    public static List<String> readLogs() {
        try {
            if (!Files.exists(LOG_FILE)) {
                return Collections.emptyList();
            }
            return Files.readAllLines(LOG_FILE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read logs", e);
        }
    }
}