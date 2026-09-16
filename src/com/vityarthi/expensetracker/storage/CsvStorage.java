package com.vityarthi.expensetracker.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the actual reading and writing of files.
 *
 * I went with plain CSV rather than a database. Partly because the whole point
 * was to keep the project dependency-free, and partly because being able to
 * open data/expenses.csv in Notepad made debugging enormously easier than
 * querying a table would have.
 */
public class CsvStorage {

    private final Path dataDir;

    public CsvStorage(String dataDirName) {
        this.dataDir = Paths.get(dataDirName);
    }

    private Path ensureFile(String fileName) throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        Path file = dataDir.resolve(fileName);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        return file;
    }

    // Returns every non-blank line. A missing file just gives an empty list,
    // which is exactly what a brand new install should see.
    public List<String> readLines(String fileName) {
        try {
            Path file = ensureFile(fileName);
            List<String> out = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    out.add(line);
                }
            }
            return out;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // Rewrites the file from scratch. Inefficient in theory, but with a few
    // hundred rows it is instant, and it avoids the mess of editing a line
    // in the middle of a text file.
    public void writeLines(String fileName, List<String> lines) throws IOException {
        Path file = ensureFile(fileName);
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public void appendLine(String fileName, String line) throws IOException {
        Path file = ensureFile(fileName);
        Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
    }

    public Path getDataDir() {
        return dataDir;
    }
}
