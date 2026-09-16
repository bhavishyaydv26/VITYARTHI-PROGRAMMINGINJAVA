package com.vityarthi.expensetracker.storage;

import com.vityarthi.expensetracker.model.Expense;
import com.vityarthi.expensetracker.model.User;
import com.vityarthi.expensetracker.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sits between the services and the raw files, converting objects to CSV rows
 * and back.
 *
 * Everything is held in memory while the program runs and written out after
 * each change. My first attempt only saved on exit, which was fine until I
 * closed the terminal window by accident and lost an evening of test data.
 */
public class DataStore {

    private static final String USERS_FILE = "users.csv";
    private static final String EXPENSES_FILE = "expenses.csv";

    private final CsvStorage storage;
    private final List<User> users = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();

    public DataStore(CsvStorage storage) {
        this.storage = storage;
        load();
    }

    private void load() {
        int skipped = 0;
        for (String line : storage.readLines(USERS_FILE)) {
            User u = User.fromCsv(line);
            if (u == null) {
                skipped++;
            } else {
                users.add(u);
            }
        }
        for (String line : storage.readLines(EXPENSES_FILE)) {
            Expense e = Expense.fromCsv(line);
            if (e == null) {
                skipped++;
            } else {
                expenses.add(e);
            }
        }
        Logger.info("Loaded " + users.size() + " users and " + expenses.size()
                + " expenses (" + skipped + " corrupt rows skipped)");
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void saveUsers() throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(u.toCsv());
        }
        storage.writeLines(USERS_FILE, lines);
    }

    public void saveExpenses() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Expense e : expenses) {
            lines.add(e.toCsv());
        }
        storage.writeLines(EXPENSES_FILE, lines);
    }

    // Next free id = biggest existing id + 1. Simple, and it survives restarts
    // because it is recalculated from whatever was loaded.
    public int nextExpenseId() {
        int max = 0;
        for (Expense e : expenses) {
            if (e.getId() > max) {
                max = e.getId();
            }
        }
        return max + 1;
    }
}
