#!/bin/bash
# WhatsApp Core LLD - MongoDB Integration Setup Script
# Run this script to build and start the application

echo ""
echo "========================================"
echo "   WhatsApp Core LLD - Setup & Build"
echo "   MongoDB Integration"
echo "========================================"
echo ""

# Check if Maven is installed
echo "[1/4] Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found! Please install Maven first."
    echo "Download from: https://maven.apache.org/download.cgi"
    exit 1
fi
echo "[✓] Maven found"

# Check if Java is installed
echo ""
echo "[2/4] Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found! Please install Java 11 or higher."
    echo "Download from: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi
echo "[✓] Java found"

# Clean and install dependencies
echo ""
echo "[3/4] Building project and downloading MongoDB driver..."
echo "This may take a few minutes on first run..."
mvn clean install -q
if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed!"
    exit 1
fi
echo "[✓] Build successful"

# Run the application
echo ""
echo "[4/4] Starting WhatsApp Core with MongoDB Integration..."
echo ""
echo "Connecting to MongoDB Atlas..."
echo "Database: whatsapp_db"
echo ""

mvn compile
mvn exec:java -Dexec.mainClass="whatsappCore"
