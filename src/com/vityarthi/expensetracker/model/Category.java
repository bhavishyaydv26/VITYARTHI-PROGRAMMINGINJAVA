package com.vityarthi.expensetracker.model;

/**
 * The spending categories a user can pick from.
 *
 * My first version let the user type the category as plain text. Within about
 * ten test entries I had "food", "Food" and "fodo" showing up as three
 * different rows in the report, so I switched to an enum. Now a wrong category
 * simply cannot be stored.
 */
public enum Category {
    FOOD,
    TRAVEL,
    RENT,
    ACADEMICS,
    SHOPPING,
    HEALTH,
    ENTERTAINMENT,
    OTHER;

    /**
     * Turns text back into a Category. Used when reading the CSV file.
     * Anything unrecognised becomes OTHER instead of blowing up - a single bad
     * row should not stop the whole app from starting.
     */
    public static Category from(String text) {
        if (text == null) {
            return OTHER;
        }
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }

    public static String listAll() {
        StringBuilder sb = new StringBuilder();
        Category[] all = values();
        for (int i = 0; i < all.length; i++) {
            sb.append(i + 1).append(") ").append(all[i].name());
            if (i < all.length - 1) {
                sb.append("  ");
            }
        }
        return sb.toString();
    }
}
