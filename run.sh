#!/bin/bash
# Compile and run the Expense Tracker (macOS / Linux)
set -e
mkdir -p out
echo "Compiling..."
javac -d out $(find src -name "*.java")
echo "Starting application..."
echo
java -cp out com.vityarthi.expensetracker.Main
