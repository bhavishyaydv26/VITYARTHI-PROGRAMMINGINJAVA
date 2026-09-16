#!/bin/bash
# Compile and run the test suite (macOS / Linux)
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.vityarthi.expensetracker.test.TestRunner
