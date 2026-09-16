package com.vityarthi.expensetracker.util;

/**
 * Thrown whenever the user gives us something we cannot accept - an empty
 * username, a negative amount, a username that is already taken.
 *
 * It is a checked exception on purpose. I wanted the compiler to force me to
 * handle every one of these at the menu level, because the earlier version
 * returned null on failure and I kept forgetting to check for it.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
