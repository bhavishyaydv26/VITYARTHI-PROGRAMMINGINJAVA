package com.vityarthi.expensetracker.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A very small logger that appends lines to data/app.log.
 *
 * I could have used java.util.logging, but its default config prints to the
 * console and that wrecked the menu layout. Writing my own five-line version
 * was easier than fighting the handler configuration.
 */
public final class Logger {

    private static final Path LOG_FILE = Paths.get("data", "app.log");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
    }

    public static void info(String message) {
        write("INFO", message);
    }

    public static void warn(String message) {
        write("WARN", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        String line = LocalDateTime.now().format(TS) + " [" + level + "] " + message
                + System.lineSeparator();
        try {
            if (LOG_FILE.getParent() != null && !Files.exists(LOG_FILE.getParent())) {
                Files.createDirectories(LOG_FILE.getParent());
            }
            Files.writeString(LOG_FILE, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Deliberately swallowed. If logging fails the user should still be
            // able to add expenses - the log is a convenience, not a feature.
        }
    }
}
