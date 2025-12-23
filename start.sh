#!/bin/bash
# WhatsApp Core LLD - Local Development Start Script
# For Mac/Linux

set -e

echo "=========================================="
echo "  WhatsApp Core LLD - Start Server"
echo "=========================================="
echo ""

# Check if jar exists
if [ ! -f "backend/target/whatsapp-lld-1.0.0.jar" ]; then
    echo "❌ JAR file not found. Please run './build.sh' first."
    exit 1
fi

echo "Starting REST Server on http://localhost:8080"
echo "WebSocket Server on ws://localhost:8081"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

cd backend
java -cp "target/whatsapp-lld-1.0.0.jar:target/dependency/*" com.whatsapp.RestServer
