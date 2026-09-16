# Personal Expense Tracker

A simple command-line application made in Java to help keep track of daily expenses.

The project does not use any frameworks or databases. It only uses Java and a few CSV files to store the data.

I made this project for the VITyarthi **"Build Your Own Project"** submission.

## Why I built it

I usually get a fixed amount of money at the beginning of the month, but somehow a large part of it is gone before the month is over. The main problem is that I don't usually spend a lot of money at once. It is more like ₹50 here, ₹100 there, some money for food, travel, recharge, printing, and other small things.

Because these expenses are small, I often don't bother writing them down. Later, when I wonder where all the money went, I don't really have an answer.

I also tried using a few expense-tracking apps. Some of them asked for SMS permissions, some showed advertisements, and one even wanted me to sign in before I could properly use it. I tried using a spreadsheet too, but after accidentally changing a formula and getting the wrong total, I stopped trusting it.

So I decided to make a small program for myself.

The main idea was simple: I wanted to be able to enter an expense in a few seconds and then check how much I had spent during the month.

## What it does

The application mainly has three things that a user can do.

**Accounts.**
A user can register using a username, password and monthly budget and then log in. The password is hashed using SHA-256 with the username used as a salt before it is stored. This means that the `users.csv` file does not contain passwords in readable form. Each user's expenses are also kept separate.

**Recording expenses.**
Users can add, view, edit and delete expenses. Every expense contains a date, category, amount and an optional note. Expenses can also be searched using keywords or filtered by category.

When entering a date, pressing Enter uses today's date. This is useful when entering several expenses one after another.

**Reports.**
The reports section is mainly there to show where the money is going. It displays the total spending, average expense, current month's spending, category-wise spending and the month-by-month spending trend.

The category and monthly trend are shown using simple bar charts made from `#` characters, so they can be viewed directly in the terminal. The application also shows the three biggest expenses.

There is also a budget warning. When spending reaches 80% of the monthly budget, the program gives a warning. If the spending reaches 100%, it tells the user that the budget has been exceeded.

## Built with

* Java 17 or newer
* JDK 21 used for development and testing
* CSV files for storage
* `java.nio.file` for file handling
* `java.security.MessageDigest` for password hashing
* `java.time` for working with dates
* VS Code for development
* Git for version control

No Maven, Gradle or external JAR files are required.



## Running it in VS Code

You need to have a JDK installed.

Open the VS Code terminal using **Ctrl + `** and check:

```bash
java -version
javac -version
```

If `javac` is not available, install a JDK such as Temurin JDK 21 and restart VS Code.

Open the project folder in VS Code and use one of the following commands.

### macOS / Linux

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.vityarthi.expensetracker.Main
```

### Windows PowerShell

```powershell
mkdir out -Force
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out com.vityarthi.expensetracker.Main
```

### Windows Command Prompt

```cmd
mkdir out
dir /s /B src\*.java > sources.txt
javac -d out @sources.txt
java -cp out com.vityarthi.expensetracker.Main
```

There are also scripts that can compile and run the project automatically:

* `run.sh` for macOS/Linux
* `run.bat` for Windows

## First run

When running the program for the first time:

1. Select `2` to register.
2. Enter a username, password and monthly budget.
3. Select `1` and log in using the same details.
4. Select `1` to add an expense.
5. At the date prompt, you can simply press Enter to use today's date.
6. Add a few expenses.
7. Select `6` to view the reports.

After using the application, a `data/` folder will be created beside the source code. It contains:

* `users.csv`
* `expenses.csv`
* `app.log`

These files can be opened using a normal text editor.

## Testing

The tests can be run using:

```bash
java -cp out com.vityarthi.expensetracker.test.TestRunner
```

There are also:

* `test.sh`
* `test.bat`

A successful test run should show:

```text
----------------------------
Passed: 26   Failed: 0
```

I did not use JUnit because I wanted the project to run using only `javac` and `java`. Instead, I made a small `TestRunner` class with a `check()` method and counters for passed and failed tests.

The tests use a separate `test-data/` folder, so they do not affect the actual application data.

The 26 tests check things such as:

* Registration and login
* Password hash storage
* Input validation
* Adding expenses
* Viewing expenses
* Editing expenses
* Deleting expenses
* User data separation
* Report calculations
* CSV reading and writing
* Handling corrupted CSV rows

I also tested some situations manually.

For example:

* Entering `abc` when an amount is expected should not crash the program.
* Entering tomorrow's date should be rejected.
* Trying to delete an ID that does not exist should show an error.
* Spending beyond the budget should show the appropriate warnings.
* If one row in `expenses.csv` is damaged, the program should continue running and skip that row.

## Design decisions I made

**Using salted hashes instead of storing plain passwords**

I did not want passwords to be stored directly in the CSV file, so I used SHA-256 with the username as a salt.

However, I know SHA-256 is not the best choice for password storage in a production application because it is very fast and can be brute-forced more easily. Password-specific algorithms such as bcrypt or PBKDF2 would be more appropriate.

I used SHA-256 because I wanted the project to work using only the standard Java libraries and without downloading external JAR files.

**Rewriting the CSV file when something changes**

Whenever an expense is changed or deleted, the CSV file is rewritten instead of trying to modify a single line in the middle of the file.

For a small project with a few hundred records, this is fast enough and keeps the implementation simpler.

**Using an enum for categories**

Initially, I allowed users to type categories manually. This created problems because entries such as `food`, `Food` and even misspelled versions could appear as different categories.

Using fixed categories avoids this problem and makes the reports more consistent.

**Saving after every change**

The program saves changes immediately instead of waiting until the user exits.

I chose this because in an earlier version, I accidentally closed the terminal and lost the expenses I had entered during that session.

## What I would add in the future

There are several things I would like to add if I continue working on this project:

* Recurring expenses so regular payments do not have to be entered every month
* Exporting reports to CSV or PDF
* Income tracking along with expenses
* Savings-rate calculation
* A proper graphical interface using JavaFX
* Moving from CSV storage to SQLite using JDBC

The storage part of the project is already separated from the rest of the application, so changing from CSV to SQLite should mainly require changes to the storage classes.

## Screenshots


<img width="693" height="379" alt="Screenshot 2026-09-16 183718" src="https://github.com/user-attachments/assets/36f9d01a-e7cb-45f7-9918-e6797c8c9d5b" />
<img width="733" height="395" alt="Screenshot 2026-09-16 183733" src="https://github.com/user-attachments/assets/9dec745a-020a-499f-8475-b10b403d03ad" />
<img width="733" height="395" alt="Screenshot 2026-09-16 183733" src="https://github.com/user-attachments/assets/6c9fe5ac-5d8e-45ee-9441-f27dba121c1a" />




