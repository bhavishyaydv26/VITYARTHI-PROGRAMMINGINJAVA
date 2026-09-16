package com.vityarthi.expensetracker;

import com.vityarthi.expensetracker.service.AuthService;
import com.vityarthi.expensetracker.service.ExpenseService;
import com.vityarthi.expensetracker.service.ReportService;
import com.vityarthi.expensetracker.storage.CsvStorage;
import com.vityarthi.expensetracker.storage.DataStore;
import com.vityarthi.expensetracker.ui.MainMenu;
import com.vityarthi.expensetracker.util.ConsoleIO;
import com.vityarthi.expensetracker.util.Logger;
import com.vityarthi.expensetracker.util.ValidationException;

/**
 * Entry point for the Expense Tracker.
 *
 * Everything gets built here once and passed downwards
 * (storage -> services -> menus). I avoided static/global objects on purpose,
 * because the test file needs to create its own copies that point at a
 * different data folder.
 *
 * @author  <Your Name>  (Reg. No. <Your Reg. No.>)
 */
public class Main {

    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO();
        CsvStorage storage = new CsvStorage("data");
        DataStore store = new DataStore(storage);

        AuthService auth = new AuthService(store);
        ExpenseService expenses = new ExpenseService(store);
        ReportService reports = new ReportService(expenses);
        MainMenu menu = new MainMenu(io, auth, expenses, reports);

        io.print("========================================");
        io.print("   Personal Expense Tracker (Java CLI)  ");
        io.print("========================================");

        boolean running = true;
        while (running) {
            io.title("Welcome");
            io.print("1. Login");
            io.print("2. Register");
            io.print("3. Exit");

            switch (io.readInt("Choose (1-3): ", 1, 3)) {
                case 1 -> {
                    if (login(io, auth)) {
                        menu.show();
                    }
                }
                case 2 -> register(io, auth);
                case 3 -> running = false;
                default -> io.error("Unknown option.");
            }
        }

        io.print("\nGoodbye. Your data is saved in the data/ folder.");
        Logger.info("Application closed");
        io.close();
    }

    private static boolean login(ConsoleIO io, AuthService auth) {
        io.title("Login");
        String username = io.readNonEmpty("Username: ");
        String password = io.readNonEmpty("Password: ");
        try {
            auth.login(username, password);
            io.success("Welcome back, " + username + "!");
            return true;
        } catch (ValidationException e) {
            io.error(e.getMessage());
            return false;
        }
    }

    private static void register(ConsoleIO io, AuthService auth) {
        io.title("Register");
        String username = io.readNonEmpty("Choose a username (min 3 chars): ");
        String password = io.readNonEmpty("Choose a password (min 4 chars): ");
        double budget = io.readAmount("Monthly budget: ");
        try {
            auth.register(username, password, budget);
            io.success("Account created. You can log in now.");
        } catch (ValidationException e) {
            io.error(e.getMessage());
        }
    }
}
