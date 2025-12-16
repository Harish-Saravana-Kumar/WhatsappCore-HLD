@echo off
REM WhatsApp REST Server Startup Script for Windows

echo ==========================================
echo   WhatsApp Web Frontend - REST Server
echo ==========================================
echo.

REM Build the project
echo Building project...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Error: Maven compilation failed
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   Starting REST Server...
echo ==========================================
echo.
echo Opening browser: http://localhost:8080
start http://localhost:8080

REM Run the REST server
java -cp "target/classes;target/dependency/*" RestServer

pause
