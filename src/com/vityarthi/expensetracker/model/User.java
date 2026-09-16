package com.vityarthi.expensetracker.model;

import java.util.Objects;

/**
 * One registered account.
 *
 * Note that the actual password is never kept anywhere - only the hash that
 * AuthService produces. If you open users.csv you will just see 64 hex
 * characters, which was the point.
 */
public class User {

    private final String username;
    private final String passwordHash;
    private double monthlyBudget;

    public User(String username, String passwordHash, double monthlyBudget) {
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.monthlyBudget = monthlyBudget;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    // One line of users.csv looks like:  rahul,8f14e45f...,8000.0
    public String toCsv() {
        return username + "," + passwordHash + "," + monthlyBudget;
    }

    // Opposite of toCsv(). Returns null if the line is damaged, and the caller
    // just skips it rather than crashing.
    public static User fromCsv(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            return null;
        }
        try {
            return new User(parts[0], parts[1], Double.parseDouble(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "User{" + username + ", budget=" + monthlyBudget + "}";
    }
}
