package com.vityarthi.expensetracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * All keyboard input goes through this class.
 *
 * I originally had Scanner calls scattered across the menus, and every one of
 * them crashed with InputMismatchException the first time I typed a letter
 * where a number was expected. Putting every read in one place with a
 * retry loop fixed that once instead of fifteen times.
 */
public class ConsoleIO {

    private final Scanner scanner;

    public ConsoleIO() {
        this.scanner = new Scanner(System.in);
    }

    public void print(String msg) {
        System.out.println(msg);
    }

    public void title(String msg) {
        System.out.println();
        System.out.println("=== " + msg + " ===");
    }

    public void error(String msg) {
        System.out.println("[!] " + msg);
    }

    public void success(String msg) {
        System.out.println("[OK] " + msg);
    }

    // Keeps asking until the user actually types something.
    public String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            if (!line.isEmpty()) {
                return line;
            }
            error("This field cannot be empty.");
        }
    }

    // For optional fields - pressing Enter keeps the fallback value.
    public String readOptional(String prompt, String fallback) {
        System.out.print(prompt);
        String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        return line.isEmpty() ? fallback : line;
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String raw = readNonEmpty(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    error("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                error("'" + raw + "' is not a whole number.");
            }
        }
    }

    public double readAmount(String prompt) {
        while (true) {
            String raw = readNonEmpty(prompt);
            try {
                double value = Double.parseDouble(raw);
                if (value <= 0) {
                    error("Amount must be greater than 0.");
                    continue;
                }
                return Math.round(value * 100.0) / 100.0;
            } catch (NumberFormatException e) {
                error("'" + raw + "' is not a valid amount.");
            }
        }
    }

    // Blank means "today", which saves a lot of typing when adding
    // several expenses in one sitting. Future dates are refused because you
    // cannot have spent money you have not spent yet.
    public LocalDate readDate(String prompt) {
        while (true) {
            String raw = readOptional(prompt, "");
            if (raw.isEmpty()) {
                return LocalDate.now();
            }
            try {
                LocalDate date = LocalDate.parse(raw);
                if (date.isAfter(LocalDate.now())) {
                    error("Date cannot be in the future.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                error("Use the format yyyy-MM-dd, e.g. 2026-09-16.");
            }
        }
    }

    public boolean confirm(String prompt) {
        String raw = readNonEmpty(prompt + " (y/n): ");
        return raw.equalsIgnoreCase("y") || raw.equalsIgnoreCase("yes");
    }

    public void close() {
        scanner.close();
    }
}
