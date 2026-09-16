package com.vityarthi.expensetracker.service;

import com.vityarthi.expensetracker.model.Category;
import com.vityarthi.expensetracker.model.Expense;
import com.vityarthi.expensetracker.model.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MODULE 3 - Reports and analytics.
 *
 * This class does nothing but arithmetic. It asks ExpenseService for the data
 * and never touches a file or the console itself, which is what makes it easy
 * to test - the report tests are just "put in three numbers, check the total".
 */
public class ReportService {

    private final ExpenseService expenseService;

    public ReportService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    public double total(String owner) {
        double sum = 0;
        for (Expense e : expenseService.listFor(owner)) {
            sum += e.getAmount();
        }
        return round(sum);
    }

    public double totalForMonth(String owner, YearMonth month) {
        double sum = 0;
        for (Expense e : expenseService.listFor(owner)) {
            if (YearMonth.from(e.getDate()).equals(month)) {
                sum += e.getAmount();
            }
        }
        return round(sum);
    }

    // Category -> total. Unused categories are left out entirely; printing
    // eight rows of zero made the report harder to read, not easier.
    public Map<Category, Double> totalsByCategory(String owner) {
        Map<Category, Double> map = new LinkedHashMap<>();
        for (Expense e : expenseService.listFor(owner)) {
            map.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        map.replaceAll((k, v) -> round(v));
        return map;
    }

    /** Month -> total, sorted chronologically. */
    public Map<YearMonth, Double> monthlyTotals(String owner) {
        Map<YearMonth, Double> map = new TreeMap<>();
        for (Expense e : expenseService.listFor(owner)) {
            map.merge(YearMonth.from(e.getDate()), e.getAmount(), Double::sum);
        }
        map.replaceAll((k, v) -> round(v));
        return map;
    }

    public List<Expense> topExpenses(String owner, int n) {
        List<Expense> all = new ArrayList<>(expenseService.listFor(owner));
        all.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        return all.subList(0, Math.min(n, all.size()));
    }

    public double averagePerEntry(String owner) {
        List<Expense> all = expenseService.listFor(owner);
        return all.isEmpty() ? 0 : round(total(owner) / all.size());
    }

    // How much of this month's budget is gone. Returns 0 when no budget is set
    // so the caller can treat "0" as "nothing to warn about".
    public double budgetUsedPercent(User user) {
        if (user.getMonthlyBudget() <= 0) {
            return 0;
        }
        double spent = totalForMonth(user.getUsername(), YearMonth.from(LocalDate.now()));
        return round(spent * 100.0 / user.getMonthlyBudget());
    }

    // Draws the bar as hashes. Anything with a non-zero value gets at least one
    // hash, otherwise small categories vanished from the chart completely.
    public String bar(double value, double max, int width) {
        if (max <= 0) {
            return "";
        }
        int filled = (int) Math.round(value / max * width);
        return "#".repeat(Math.max(filled, value > 0 ? 1 : 0));
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
