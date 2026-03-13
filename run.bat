@echo off
setlocal

:: ── Always run from the bat file's own directory ──────────────────
cd /d "%~dp0"

echo.
echo  ============================================
echo    CPU Scheduling Simulator
echo  ============================================
echo    Folder: %CD%
echo.

:: ── Check Java is installed ───────────────────────────────────────
java -version >nul 2>&1
if errorlevel 1 (
    echo  [ERROR] Java not found.
    echo          Install JDK 11+ from:
    echo          https://adoptium.net
    echo.
    pause
    exit /b 1
)

javac -version >nul 2>&1
if errorlevel 1 (
    echo  [ERROR] javac not found.
    echo          You have the JRE, but you need the full JDK.
    echo.
    pause
    exit /b 1
)

:: ── Create bin folder ─────────────────────────────────────────────
if not exist bin mkdir bin

:: ── Compile all Java source files ─────────────────────────────────
echo  [1/2] Compiling sources...
javac -d bin src\Process.java src\GanttBlock.java src\SchedulingResult.java src\FCFS.java src\RoundRobin.java src\ShortestProcessNext.java src\ShortestRemainingTime.java src\PriorityScheduling.java src\ScheduleHandler.java src\Scheduler.java

if errorlevel 1 (
    echo.
    echo  [ERROR] Compilation failed. Read the error above.
    pause
    exit /b 1
)

echo  [2/2] Compiled successfully!
echo.
echo  ============================================
echo    Open http://localhost:8080 in your browser
echo    Press Ctrl+C to stop the server.
echo  ============================================
echo.

:: ── Auto-open browser after 2 seconds ────────────────────────────
start "" /b cmd /c "ping -n 3 127.0.0.1 >nul && start http://localhost:8080"

:: ── Start server ──────────────────────────────────────────────────
java -cp bin Scheduler

echo.
echo  Server stopped.
pause
endlocal
