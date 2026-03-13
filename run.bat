@echo off
echo ============================================
echo   CPU Scheduling Simulator - Build & Run
echo ============================================

:: Create output directory
if not exist "bin" mkdir bin

echo [1/2] Compiling Java sources...
javac -d bin src\Process.java src\GanttBlock.java src\SchedulingResult.java src\FCFS.java src\RoundRobin.java src\ShortestProcessNext.java src\ShortestRemainingTime.java src\PriorityScheduling.java src\ScheduleHandler.java src\Scheduler.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Make sure Java JDK is installed.
    echo         Run: java -version  to check.
    pause
    exit /b 1
)

echo [2/2] Compilation successful! Starting server...
echo.
echo  Open your browser at: http://localhost:8080
echo  Press Ctrl+C to stop the server.
echo.

java -cp bin Scheduler

pause
