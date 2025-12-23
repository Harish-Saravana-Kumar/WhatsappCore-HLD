#!/bin/bash
set -e

echo "Starting WhatsApp Backend on Render..."

# Run the application using java -jar with dependencies from root directory
java -cp "backend/target/whatsapp-lld-1.0.0.jar:backend/target/dependency/*" com.whatsapp.RestServer
