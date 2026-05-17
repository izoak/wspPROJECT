package logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public final class ActionLogger {
    private static final Path LOG_DIRECTORY = Path.of("logs");
    private static final Path LOG_FILE = LOG_DIRECTORY.resolve("system.log");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ActionLogger() {
    }

    public static void log(String message) {
        try {
            Files.createDirectories(LOG_DIRECTORY);
            String line = "[" + LocalDateTime.now().format(FORMATTER) + "] " + message + System.lineSeparator();
            Files.writeString(LOG_FILE, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write log entry", e);
        }
    }

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
