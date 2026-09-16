package com.vityarthi.expensetracker.service;

import com.vityarthi.expensetracker.model.User;
import com.vityarthi.expensetracker.storage.DataStore;
import com.vityarthi.expensetracker.util.Logger;
import com.vityarthi.expensetracker.util.ValidationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MODULE 1 - User management.
 *
 * Registration, login, logout and budget changes live here. Passwords are
 * salted with the username and hashed with SHA-256 before being stored. Two
 * people who happen to pick the same password still end up with different
 * hashes, which is the reason for the salt.
 *
 * (In a real product this should be bcrypt or PBKDF2 with many iterations -
 * SHA-256 is fast, which is a disadvantage for passwords. But bcrypt needs an
 * external jar and the project had to stay dependency-free.)
 */
public class AuthService {

    private final DataStore store;
    private User currentUser;

    public AuthService(DataStore store) {
        this.store = store;
    }

    public User register(String username, String password, double monthlyBudget)
            throws ValidationException {
        validateUsername(username);
        validatePassword(password);
        if (monthlyBudget < 0) {
            throw new ValidationException("Monthly budget cannot be negative.");
        }
        if (findUser(username) != null) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }

        User user = new User(username, hash(username, password), monthlyBudget);
        store.getUsers().add(user);
        persistUsers();
        Logger.info("Registered new user: " + username);
        return user;
    }

    public User login(String username, String password) throws ValidationException {
        User user = findUser(username);
        if (user == null || !user.getPasswordHash().equals(hash(username, password))) {
            Logger.warn("Failed login attempt for: " + username);
            throw new ValidationException("Invalid username or password.");
        }
        currentUser = user;
        Logger.info("Login success: " + username);
        return user;
    }

    public void logout() {
        if (currentUser != null) {
            Logger.info("Logout: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public void updateBudget(double newBudget) throws ValidationException {
        if (currentUser == null) {
            throw new ValidationException("No user is logged in.");
        }
        if (newBudget < 0) {
            throw new ValidationException("Monthly budget cannot be negative.");
        }
        currentUser.setMonthlyBudget(newBudget);
        persistUsers();
        Logger.info("Budget updated for " + currentUser.getUsername() + " to " + newBudget);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User findUser(String username) {
        for (User u : store.getUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    private void validateUsername(String username) throws ValidationException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (username.length() < 3) {
            throw new ValidationException("Username must be at least 3 characters.");
        }
        if (!username.matches("[A-Za-z0-9_]+")) {
            throw new ValidationException("Username may only contain letters, digits and underscore.");
        }
    }

    private void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters.");
        }
    }

    private void persistUsers() {
        try {
            store.saveUsers();
        } catch (IOException e) {
            Logger.error("Could not save users.csv: " + e.getMessage());
        }
    }

    // Salted hash as lowercase hex. Package-private so the tests can check
    // that the stored value really is not the plain password.
    static String hash(String username, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((username + "::" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }
}
