#!/bin/bash

# WhatsApp REST Server Startup Script for Linux/Mac

echo "=========================================="
echo "   WhatsApp Web Frontend - REST Server"
echo "=========================================="
echo ""

# Build the project
echo "Building project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Error: Maven compilation failed"
    exit 1
fi

echo ""
echo "=========================================="
echo "   Starting REST Server..."
echo "=========================================="
echo ""
echo "Opening browser: http://localhost:8080"

# Run the REST server
java -cp "target/classes:target/dependency/*" RestServer
