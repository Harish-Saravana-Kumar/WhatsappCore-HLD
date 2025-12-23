#!/bin/bash
set -e

echo "Building WhatsApp Backend for Render..."

cd backend
mvn clean package -DskipTests

echo "Build completed successfully!"
