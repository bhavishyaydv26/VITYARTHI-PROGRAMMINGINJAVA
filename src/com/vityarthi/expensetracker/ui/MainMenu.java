package com.vityarthi.expensetracker.ui;

import com.vityarthi.expensetracker.model.Category;
import com.vityarthi.expensetracker.model.Expense;
import com.vityarthi.expensetracker.model.User;
import com.vityarthi.expensetracker.service.AuthService;
import com.vityarthi.expensetracker.service.ExpenseService;
import com.vityarthi.expensetracker.service.ReportService;
import com.vityarthi.expensetracker.util.ConsoleIO;
import com.vityarthi.expensetracker.util.ValidationException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * The menus a user sees after logging in.
 *
 * This class is deliberately dumb - it asks questions, calls a service, and
 * prints whatever comes back. There is no arithmetic and no file access in
 * here. Keeping it that way is what stopped the project turning into one
 * enormous Main method, which is roughly what happened in my first draft.
 */
public class MainMenu {

    private final ConsoleIO io;
    private final AuthService auth;
    private final ExpenseService expenses;
    private final ReportService reports;

    public MainMenu(ConsoleIO io, AuthService auth, ExpenseService expenses, ReportService reports) {
        this.io = io;
        this.auth = auth;
        this.expenses = expenses;
        this.reports = reports;
    }

    // Loops until the user logs out. auth.isLoggedIn() is the exit condition
    // so option 8 does not need a break flag of its own.
    public void show() {
        while (auth.isLoggedIn()) {
            User user = auth.getCurrentUser();
            io.title("Main Menu  (" + user.getUsername() + ")");
            io.print("1. Add expense");
            io.print("2. View all expenses");
            io.print("3. Update an expense");
            io.print("4. Delete an expense");
            io.print("5. Search / filter expenses");
            io.print("6. Reports & analytics");
            io.print("7. Change monthly budget");
            io.print("8. Logout");

            switch (io.readInt("Choose (1-8): ", 1, 8)) {
                case 1 -> addExpense();
                case 2 -> viewAll();
                case 3 -> updateExpense();
                case 4 -> deleteExpense();
                case 5 -> searchMenu();
                case 6 -> reportMenu();
                case 7 -> changeBudget();
                case 8 -> auth.logout();
                default -> io.error("Unknown option.");
            }
        }
    }

    private void addExpense() {
        io.title("Add Expense");
        LocalDate date = io.readDate("Date yyyy-MM-dd (Enter = today): ");
        Category category = askCategory();
        double amount = io.readAmount("Amount: ");
        String note = io.readOptional("Note (optional): ", "");
        try {
            Expense e = expenses.add(auth.getCurrentUser().getUsername(), date, category, amount, note);
            io.success("Saved expense #" + e.getId());
            warnIfOverBudget();
        } catch (ValidationException ex) {
            io.error(ex.getMessage());
        }
    }

    private void viewAll() {
        io.title("All Expenses");
        printTable(expenses.listFor(auth.getCurrentUser().getUsername()));
    }

    private void updateExpense() {
        io.title("Update Expense");
        List<Expense> list = expenses.listFor(auth.getCurrentUser().getUsername());
        if (list.isEmpty()) {
            io.print("Nothing to update yet.");
            return;
        }
        printTable(list);
        int id = io.readInt("Id to update: ", 1, Integer.MAX_VALUE);
        Expense existing = expenses.findById(auth.getCurrentUser().getUsername(), id);
        if (existing == null) {
            io.error("No expense with id " + id + ".");
            return;
        }
        LocalDate date = io.readDate("New date yyyy-MM-dd (Enter = today): ");
        Category category = askCategory();
        double amount = io.readAmount("New amount: ");
        String note = io.readOptional("New note (Enter keeps '" + existing.getNote() + "'): ",
                existing.getNote());
        try {
            expenses.update(auth.getCurrentUser().getUsername(), id, date, category, amount, note);
            io.success("Expense #" + id + " updated.");
        } catch (ValidationException ex) {
            io.error(ex.getMessage());
        }
    }

    private void deleteExpense() {
        io.title("Delete Expense");
        List<Expense> list = expenses.listFor(auth.getCurrentUser().getUsername());
        if (list.isEmpty()) {
            io.print("Nothing to delete yet.");
            return;
        }
        printTable(list);
        int id = io.readInt("Id to delete: ", 1, Integer.MAX_VALUE);
        if (!io.confirm("Delete expense #" + id + "?")) {
            io.print("Cancelled.");
            return;
        }
        try {
            expenses.delete(auth.getCurrentUser().getUsername(), id);
            io.success("Expense #" + id + " deleted.");
        } catch (ValidationException ex) {
            io.error(ex.getMessage());
        }
    }

    private void searchMenu() {
        io.title("Search / Filter");
        io.print("1. Keyword search");
        io.print("2. Filter by category");
        String owner = auth.getCurrentUser().getUsername();
        if (io.readInt("Choose (1-2): ", 1, 2) == 1) {
            String keyword = io.readNonEmpty("Keyword: ");
            printTable(expenses.search(owner, keyword));
        } else {
            printTable(expenses.filterByCategory(owner, askCategory()));
        }
    }

    private void reportMenu() {
        String owner = auth.getCurrentUser().getUsername();
        io.title("Reports & Analytics");

        io.print("Total spent      : " + money(reports.total(owner)));
        io.print("Average / entry  : " + money(reports.averagePerEntry(owner)));
        io.print("This month       : " + money(reports.totalForMonth(owner, YearMonth.now())));

        Map<Category, Double> byCategory = reports.totalsByCategory(owner);
        if (byCategory.isEmpty()) {
            io.print("\nNo expenses recorded yet.");
            return;
        }

        double max = byCategory.values().stream().max(Double::compare).orElse(0.0);
        io.print("\n-- Spending by category --");
        for (Map.Entry<Category, Double> entry : byCategory.entrySet()) {
            io.print(String.format("%-14s %12s  %s", entry.getKey(), money(entry.getValue()),
                    reports.bar(entry.getValue(), max, 30)));
        }

        io.print("\n-- Monthly totals --");
        Map<YearMonth, Double> monthly = reports.monthlyTotals(owner);
        double maxMonth = monthly.values().stream().max(Double::compare).orElse(0.0);
        for (Map.Entry<YearMonth, Double> entry : monthly.entrySet()) {
            io.print(String.format("%-14s %12s  %s", entry.getKey(), money(entry.getValue()),
                    reports.bar(entry.getValue(), maxMonth, 30)));
        }

        io.print("\n-- Top 3 expenses --");
        printTable(reports.topExpenses(owner, 3));

        warnIfOverBudget();
    }

    private void changeBudget() {
        io.title("Monthly Budget");
        io.print("Current budget: " + money(auth.getCurrentUser().getMonthlyBudget()));
        double budget = io.readAmount("New monthly budget: ");
        try {
            auth.updateBudget(budget);
            io.success("Budget updated.");
        } catch (ValidationException ex) {
            io.error(ex.getMessage());
        }
    }

    private void warnIfOverBudget() {
        User user = auth.getCurrentUser();
        double used = reports.budgetUsedPercent(user);
        if (used == 0) {
            return;
        }
        String line = String.format("Budget used this month: %.1f%% of %s", used,
                money(user.getMonthlyBudget()));
        if (used >= 100) {
            io.error(line + "  -- budget exceeded!");
        } else if (used >= 80) {
            io.error(line + "  -- approaching your limit.");
        } else {
            io.print(line);
        }
    }

    private Category askCategory() {
        io.print("Categories: " + Category.listAll());
        int choice = io.readInt("Category number: ", 1, Category.values().length);
        return Category.values()[choice - 1];
    }

    private void printTable(List<Expense> list) {
        if (list.isEmpty()) {
            io.print("No matching expenses.");
            return;
        }
        io.print(String.format("%-5s %-12s %-14s %12s  %s", "ID", "DATE", "CATEGORY", "AMOUNT", "NOTE"));
        io.print("-".repeat(70));
        double sum = 0;
        for (Expense e : list) {
            io.print(e.toRow());
            sum += e.getAmount();
        }
        io.print("-".repeat(70));
        io.print(String.format("%-33s %12s", "TOTAL (" + list.size() + " entries)", money(sum)));
    }

    private String money(double value) {
        return String.format("Rs %.2f", value);
    }
}
