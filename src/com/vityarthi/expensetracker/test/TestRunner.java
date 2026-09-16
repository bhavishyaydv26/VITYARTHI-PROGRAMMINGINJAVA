package com.vityarthi.expensetracker.test;

import com.vityarthi.expensetracker.model.Category;
import com.vityarthi.expensetracker.model.Expense;
import com.vityarthi.expensetracker.model.User;
import com.vityarthi.expensetracker.service.AuthService;
import com.vityarthi.expensetracker.service.ExpenseService;
import com.vityarthi.expensetracker.service.ReportService;
import com.vityarthi.expensetracker.storage.CsvStorage;
import com.vityarthi.expensetracker.storage.DataStore;
import com.vityarthi.expensetracker.util.ValidationException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

/**
 * My own little test harness.
 *
 * JUnit would have been nicer, but it needs jars on the classpath and the whole
 * point of this project was that a marker can run it with nothing but javac and
 * java. So: a check() method, two counters, and a non-zero exit code if
 * anything fails.
 *
 * Everything runs against test-data/ rather than data/, so running the tests
 * never touches real expenses.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Running tests...\n");

        DataStore store = new DataStore(new CsvStorage("test-data"));
        store.getUsers().clear();
        store.getExpenses().clear();

        AuthService auth = new AuthService(store);
        ExpenseService expenses = new ExpenseService(store);
        ReportService reports = new ReportService(expenses);

        testRegistrationAndLogin(auth);
        testValidationRules(auth, expenses);
        testCrud(expenses);
        testReports(reports, expenses);
        testCsvRoundTrip();

        System.out.println("\n----------------------------");
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testRegistrationAndLogin(AuthService auth) {
        try {
            User u = auth.register("tester", "pass123", 10000);
            check("register creates user", u != null && "tester".equals(u.getUsername()));
            check("password is not stored in plain text", !u.getPasswordHash().contains("pass123"));
            auth.login("tester", "pass123");
            check("login with correct password", auth.isLoggedIn());
        } catch (ValidationException e) {
            check("registration/login flow (" + e.getMessage() + ")", false);
        }
        check("login with wrong password is rejected",
                throwsValidation(() -> auth.login("tester", "wrong")));
        check("duplicate username is rejected",
                throwsValidation(() -> auth.register("tester", "abcd", 100)));
    }

    private static void testValidationRules(AuthService auth, ExpenseService expenses) {
        check("short username rejected", throwsValidation(() -> auth.register("ab", "abcd", 10)));
        check("short password rejected", throwsValidation(() -> auth.register("valid_user", "ab", 10)));
        check("negative amount rejected", throwsValidation(
                () -> expenses.add("tester", LocalDate.now(), Category.FOOD, -50, "bad")));
        check("future date rejected", throwsValidation(
                () -> expenses.add("tester", LocalDate.now().plusDays(3), Category.FOOD, 50, "bad")));
    }

    private static void testCrud(ExpenseService expenses) {
        try {
            Expense a = expenses.add("tester", LocalDate.now(), Category.FOOD, 250.0, "Lunch");
            expenses.add("tester", LocalDate.now().minusDays(1), Category.TRAVEL, 120.0, "Bus pass");
            expenses.add("other_user", LocalDate.now(), Category.RENT, 9000.0, "Rent");

            check("create + list returns only own records", expenses.listFor("tester").size() == 2);
            check("ids are unique", a.getId() != expenses.listFor("tester").get(0).getId()
                    || expenses.listFor("tester").size() == 2);

            expenses.update("tester", a.getId(), LocalDate.now(), Category.FOOD, 300.0, "Lunch updated");
            check("update changes the amount",
                    expenses.findById("tester", a.getId()).getAmount() == 300.0);

            check("search finds by keyword", expenses.search("tester", "bus").size() == 1);
            check("filter by category works",
                    expenses.filterByCategory("tester", Category.TRAVEL).size() == 1);

            check("cannot touch another user's record",
                    expenses.findById("tester", 3) == null);

            expenses.delete("tester", a.getId());
            check("delete removes the record", expenses.listFor("tester").size() == 1);
        } catch (ValidationException e) {
            check("CRUD flow (" + e.getMessage() + ")", false);
        }
    }

    private static void testReports(ReportService reports, ExpenseService expenses) {
        try {
            expenses.add("reportuser", LocalDate.now(), Category.FOOD, 100.0, "a");
            expenses.add("reportuser", LocalDate.now(), Category.FOOD, 150.0, "b");
            expenses.add("reportuser", LocalDate.now(), Category.TRAVEL, 250.0, "c");
        } catch (ValidationException e) {
            check("report setup", false);
            return;
        }
        check("total sums every entry", reports.total("reportuser") == 500.0);
        check("average per entry", reports.averagePerEntry("reportuser") == 166.67);

        Map<Category, Double> byCat = reports.totalsByCategory("reportuser");
        check("category grouping", byCat.get(Category.FOOD) == 250.0
                && byCat.get(Category.TRAVEL) == 250.0);
        check("monthly total for current month",
                reports.totalForMonth("reportuser", YearMonth.now()) == 500.0);
        check("top expenses are sorted desc",
                reports.topExpenses("reportuser", 2).get(0).getAmount() == 250.0);
        check("empty user totals to zero", reports.total("nobody") == 0.0);
    }

    private static void testCsvRoundTrip() {
        Expense e = new Expense(7, "tester", LocalDate.of(2026, 5, 4), Category.HEALTH, 99.5, "Medicine, tablets");
        Expense back = Expense.fromCsv(e.toCsv());
        check("expense survives CSV round trip", back != null
                && back.getId() == 7
                && back.getCategory() == Category.HEALTH
                && back.getAmount() == 99.5);
        check("commas are stripped from notes", !e.getNote().contains(","));
        check("corrupt row returns null", Expense.fromCsv("1,broken,row") == null);
        check("user survives CSV round trip",
                User.fromCsv(new User("u", "h", 500).toCsv()).getMonthlyBudget() == 500.0);
    }

    // ---- the entire "framework" ------------------------------------------

    private interface Risky {
        void run() throws ValidationException;
    }

    private static boolean throwsValidation(Risky r) {
        try {
            r.run();
            return false;
        } catch (ValidationException e) {
            return true;
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }
}
