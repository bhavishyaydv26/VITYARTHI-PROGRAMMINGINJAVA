package com.vityarthi.expensetracker.service;

import com.vityarthi.expensetracker.model.Category;
import com.vityarthi.expensetracker.model.Expense;
import com.vityarthi.expensetracker.storage.DataStore;
import com.vityarthi.expensetracker.util.Logger;
import com.vityarthi.expensetracker.util.ValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MODULE 2 - The expense records themselves (create, read, update, delete).
 *
 * Every single method filters on the owner. I put that check down here rather
 * than in the menus, because if I ever add a second interface the isolation
 * still holds - a bug in the UI cannot expose somebody else's spending.
 */
public class ExpenseService {

    private final DataStore store;

    public ExpenseService(DataStore store) {
        this.store = store;
    }

    public Expense add(String owner, LocalDate date, Category category, double amount, String note)
            throws ValidationException {
        if (owner == null || owner.isBlank()) {
            throw new ValidationException("An expense must belong to a user.");
        }
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than 0.");
        }
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new ValidationException("Date must be valid and not in the future.");
        }
        Expense expense = new Expense(store.nextExpenseId(), owner, date,
                category == null ? Category.OTHER : category, amount, note);
        store.getExpenses().add(expense);
        persist();
        Logger.info("Added expense #" + expense.getId() + " for " + owner);
        return expense;
    }

    // Newest first, because that is what you want to see when you open the
    // list. Ties are broken by id so the order never jumps around.
    public List<Expense> listFor(String owner) {
        List<Expense> out = new ArrayList<>();
        for (Expense e : store.getExpenses()) {
            if (e.getOwner().equalsIgnoreCase(owner)) {
                out.add(e);
            }
        }
        out.sort(Comparator.comparing(Expense::getDate).reversed()
                .thenComparing(Expense::getId, Comparator.reverseOrder()));
        return out;
    }

    public Expense findById(String owner, int id) {
        for (Expense e : store.getExpenses()) {
            if (e.getId() == id && e.getOwner().equalsIgnoreCase(owner)) {
                return e;
            }
        }
        return null;
    }

    public void update(String owner, int id, LocalDate date, Category category,
                       double amount, String note) throws ValidationException {
        Expense e = findById(owner, id);
        if (e == null) {
            throw new ValidationException("No expense found with id " + id + ".");
        }
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than 0.");
        }
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new ValidationException("Date must be valid and not in the future.");
        }
        e.setDate(date);
        e.setCategory(category);
        e.setAmount(amount);
        e.setNote(note);
        persist();
        Logger.info("Updated expense #" + id);
    }

    public void delete(String owner, int id) throws ValidationException {
        Expense e = findById(owner, id);
        if (e == null) {
            throw new ValidationException("No expense found with id " + id + ".");
        }
        store.getExpenses().remove(e);
        persist();
        Logger.info("Deleted expense #" + id);
    }

    // Case-insensitive search over the note and the category name. Not indexed
    // or anything clever - just a scan, which is plenty for this size of data.
    public List<Expense> search(String owner, String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase();
        List<Expense> out = new ArrayList<>();
        for (Expense e : listFor(owner)) {
            if (e.getNote().toLowerCase().contains(k)
                    || e.getCategory().name().toLowerCase().contains(k)) {
                out.add(e);
            }
        }
        return out;
    }

    public List<Expense> filterByCategory(String owner, Category category) {
        List<Expense> out = new ArrayList<>();
        for (Expense e : listFor(owner)) {
            if (e.getCategory() == category) {
                out.add(e);
            }
        }
        return out;
    }

    private void persist() {
        try {
            store.saveExpenses();
        } catch (IOException e) {
            Logger.error("Could not save expenses.csv: " + e.getMessage());
        }
    }
}
