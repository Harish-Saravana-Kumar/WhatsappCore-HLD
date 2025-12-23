@echo off
REM WhatsApp Core LLD - MongoDB Integration Setup Script
REM Run this script to build and start the application

echo.
echo ========================================
echo   WhatsApp Core LLD - Setup & Build
echo   MongoDB Integration
echo ========================================
echo.

REM Check if Maven is installed
echo [1/4] Checking Maven installation...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found! Please install Maven first.
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo [✓] Maven found

REM Check if Java is installed
echo.
echo [2/4] Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found! Please install Java 11 or higher.
    echo Download from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)
echo [✓] Java found

REM Clean and install dependencies
echo.
echo [3/4] Building project and downloading MongoDB driver...
echo This may take a few minutes on first run...
mvn clean install -q
if errorlevel 1 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo [✓] Build successful

REM Run the application
echo.
echo [4/4] Starting WhatsApp Core with MongoDB Integration...
echo.
echo Connecting to MongoDB Atlas...
echo Database: whatsapp_db
echo.
pause

mvn compile
mvn exec:java -Dexec.mainClass="whatsappCore"

pause
