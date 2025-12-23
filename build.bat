@echo off
REM WhatsApp Core LLD - Local Development Build Script
REM For Windows

echo ==========================================
echo   WhatsApp Core LLD - Build Script
echo ==========================================
echo.

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found!
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java 17+ from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo Maven and Java found
echo.

REM Build the backend
echo Building backend...
cd backend
call mvn clean package -DskipTests

if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.
echo JAR file created: target\whatsapp-lld-1.0.0.jar
echo Dependencies copied to: target\dependency\
pause
