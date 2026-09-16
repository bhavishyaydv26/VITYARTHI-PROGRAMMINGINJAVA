@echo off
if not exist out mkdir out
dir /s /B src\*.java > sources.txt
javac -d out @sources.txt
java -cp out com.vityarthi.expensetracker.test.TestRunner
del sources.txt 2>nul
pause
