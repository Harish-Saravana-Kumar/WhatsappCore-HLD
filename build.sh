#!/bin/bash
# WhatsApp Core LLD - Local Development Build Script
# For Mac/Linux

set -e

echo "=========================================="
echo "  WhatsApp Core LLD - Build Script"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven not found!"
    echo "Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java not found!"
    echo "Please install Java 17+ from: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

echo "✓ Maven and Java found"
echo ""

# Build the backend
echo "Building backend..."
cd backend
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build successful!"
    echo ""
    echo "JAR file created: target/whatsapp-lld-1.0.0.jar"
    echo "Dependencies copied to: target/dependency/"
else
    echo "❌ Build failed!"
    exit 1
fi
