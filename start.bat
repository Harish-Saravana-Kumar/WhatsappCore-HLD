@echo off
REM WhatsApp Core LLD - Local Development Start Script
REM For Windows

echo ==========================================
echo   WhatsApp Core LLD - Start Server
echo ==========================================
echo.

REM Check if jar exists
if not exist "backend\target\whatsapp-lld-1.0.0.jar" (
    echo ERROR: JAR file not found. Please run 'build.bat' first.
    pause
    exit /b 1
)

echo Starting REST Server on http://localhost:8080
echo WebSocket Server on ws://localhost:8081
echo.
echo Press Ctrl+C to stop the server
echo.

cd backend
java -cp "target\whatsapp-lld-1.0.0.jar;target\dependency\*" com.whatsapp.RestServer
pause
