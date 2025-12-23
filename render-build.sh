#!/bin/bash
set -e

echo "Building WhatsApp Backend for Render..."

# Navigate to backend directory
cd backend

# Build the application
echo "Running Maven build..."
mvn clean package -DskipTests

echo "Build completed successfully!"
