#!/bin/bash
set -e

echo "Starting WhatsApp Backend on Render..."

# Navigate to backend directory
cd backend

# Run the application using java -jar with dependencies
java -cp "target/whatsapp-lld-1.0.0.jar:target/dependency/*" com.whatsapp.RestServer
