package com.vityarthi.expensetracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * A single spending entry belonging to one user.
 *
 * Notes get their commas stripped before saving. I found this out the hard way:
 * typing "Lunch, chai and samosa" turned one CSV row into eight columns and the
 * whole file failed to load the next time I opened the app.
 */
public class Expense {

    private final int id;
    private final String owner;
    private LocalDate date;
    private Category category;
    private double amount;
    private String note;

    public Expense(int id, String owner, LocalDate date, Category category, double amount, String note) {
        this.id = id;
        this.owner = owner;
        this.date = date;
        this.category = category;
        this.amount = amount;
        setNote(note);
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public final void setNote(String note) {
        this.note = (note == null) ? "" : note.replace(',', ' ').trim();
    }

    public String toCsv() {
        return id + "," + owner + "," + date + "," + category.name() + "," + amount + "," + note;
    }

    public static Expense fromCsv(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] p = line.split(",", -1);
        if (p.length != 6) {
            return null;
        }
        try {
            return new Expense(
                    Integer.parseInt(p[0]),
                    p[1],
                    LocalDate.parse(p[2]),
                    Category.from(p[3]),
                    Double.parseDouble(p[4]),
                    p[5]);
        } catch (NumberFormatException | DateTimeParseException e) {
            // Damaged line - hand back null so DataStore can skip just this row.
            return null;
        }
    }

    // Fixed-width version for the on-screen table. The widths here have to match
    // the header printed in MainMenu.printTable() or the columns look crooked.
    public String toRow() {
        return String.format("%-5d %-12s %-14s %12.2f  %s",
                id, date, category.name(), amount, note);
    }

    @Override
    public String toString() {
        return toRow();
    }
}
