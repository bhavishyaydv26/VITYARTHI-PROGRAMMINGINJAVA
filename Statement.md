# Problem Statement

## The problem

Students often spend money in small amounts throughout the day.

For example, it could be ₹50 at the mess, ₹30 for an auto, ₹200 for a recharge, or ₹100 for printing before submitting an assignment. None of these expenses feels very large on its own, so it is easy to ignore them or forget about them.

The problem usually becomes visible later in the month. By around the twentieth, a large part of the monthly allowance may already be gone, but it can be difficult to remember exactly where the money was spent.

## Why existing options are not always suitable

There are already many expense-tracking applications, but they are not always convenient for students.

Many mobile expense apps are designed around bank accounts and regular income. Some ask for SMS permissions so that they can automatically read transaction messages. Others require an account before the user can start entering expenses, and some display advertisements.

For someone who only wants to record small daily expenses, this can feel unnecessary. It also means that financial information may be stored or processed by another service.

Spreadsheets are another option. They give users a lot of flexibility, but they also require the user to maintain formulas and organize the data themselves. If a formula is accidentally changed, the totals can become incorrect without being noticed immediately.

A paper diary can be useful for writing down expenses, but calculating totals or checking how much was spent in a particular category becomes difficult.

## The idea behind the project

The main issue is not simply recording expenses.

If I write down ten expenses but never look at the total or categories, the list does not really help me make better decisions.

The project therefore focuses on three things:

- Entering an expense quickly
- Keeping the information private and stored locally
- Turning the recorded expenses into useful reports

For example, instead of only seeing a list of numbers, the application can show that a certain percentage of the monthly budget has already been used and which category has received the most spending.

That is the main purpose of this project.

## Scope

### What the application does

The application provides:

- Multiple user accounts with password-protected login
- Adding, viewing, editing and deleting expenses
- Eight fixed spending categories
- Keyword search
- Category filtering
- A monthly budget
- Budget warnings at 80% and 100%
- Total and average spending calculations
- Category-wise spending breakdown
- Monthly spending trends
- The three largest expenses
- Simple text-based bar charts
- Local CSV storage
- An activity log
- Input validation
- Automated tests

### What the application does not do

The project is intentionally kept offline and simple.

It does not include:

- Bank integration
- UPI integration
- SMS integration
- Currency conversion
- Cloud synchronization
- A graphical interface
- Income tracking
- Loan tracking
- Investment tracking

The application does not connect to the internet. All expense information remains on the computer where the program is being used.

## Who it is for

The main user I had in mind while building the project was a hostel student who receives a fixed monthly allowance of around ₹8,000 and wants to keep track of spending during the month.

For example, the application can help answer a simple question such as: "How much money have I already spent this month, and where did most of it go?"

The project can also be useful for other people with similar needs, such as:

- Students living in PGs
- People trying to control their monthly spending
- Someone starting their first job and developing a habit of tracking expenses
- People who prefer keeping their financial information on their own computer instead of storing it on an online service

There is also another possible use of the project. Since the Java code is divided into different layers and contains comments, it can be useful as a learning example for someone who is trying to understand how a Java application can be organized.

## Main features

### Module 1 — Users

The user module handles registration and login.

A new user provides:

- Username
- Password
- Monthly budget

Passwords are salted using the username and hashed with SHA-256 before being stored. Therefore, the actual password is not stored as readable text in the CSV file.

After logging in, the application only works with the expenses belonging to that user. One user cannot access another user's expense records.

The monthly budget can also be changed later.

### Module 2 — Expenses

The expense module is used for managing individual expenses.

When creating an expense, the user enters:

- Date
- Category
- Amount
- Optional note

Expenses are displayed with the newest entries first, along with a running total.

The user can also:

- Edit an existing expense
- Delete an expense
- Search by keyword
- Filter by category

Before deleting an expense, the program asks for confirmation because the deletion cannot be undone.

### Module 3 — Reports

The reports module converts the stored expenses into useful information.

It displays:

- Total spending
- Average expense
- Current month's spending
- Category-wise spending
- Monthly spending trend
- Three largest expenses
- Current budget status

The category breakdown and monthly trend are displayed using simple bar charts made from text characters, so they work directly in the terminal.

The budget status changes when spending reaches important limits. A warning is shown after 80% of the budget has been used, and a clear message is shown once the budget has been exceeded.

## Other supporting features

The main modules are supported by a few additional features.

The application stores data in CSV files so that information remains available after the program is closed and opened again.

There is also an activity log stored at:

```text
data/app.log
```

Input validation is included throughout the application. If the user enters something incorrectly, the program asks for the input again instead of immediately crashing.

The project also contains 26 automated tests covering the main functionality.

## Input and output

| What the user is doing | What the user enters | What the application provides |
|---|---|---|
| Registering | Username, password, budget | A new user record in `users.csv` |
| Logging in | Username and password | A login session or an error message |
| Adding an expense | Date, category, amount, note | A saved expense and budget status |
| Viewing/searching | Optional keyword or category | Matching expenses and total |
| Updating | Expense ID and new values | Updated and saved expense |
| Deleting | Expense ID and confirmation | Expense removed |
| Viewing reports | No additional input | Totals, charts, top expenses and budget status |