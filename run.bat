@echo off
REM Compile and run the Expense Tracker (Windows)
if not exist out mkdir out
echo Compiling...
dir /s /B src\*.java > sources.txt
javac -d out @sources.txt
if errorlevel 1 goto error
echo Starting application...
echo.
java -cp out com.vityarthi.expensetracker.Main
goto end
:error
echo Compilation failed.
:end
del sources.txt 2>nul
pause
