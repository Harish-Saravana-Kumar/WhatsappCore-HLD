#!/bin/bash
set -e

echo "Building WhatsApp Backend for Render..."

# Check if java is available in PATH
if ! command -v java &> /dev/null; then
    echo "Java not found in PATH, searching for installation..."
    
    # Search for Java in common locations
    for java_path in /usr/lib/jvm/*/bin/java /opt/*/bin/java; do
        if [ -x "$java_path" ]; then
            export JAVA_HOME=$(dirname $(dirname "$java_path"))
            export PATH=$JAVA_HOME/bin:$PATH
            echo "Found Java at: $JAVA_HOME"
            break
        fi
    done
    
    # If still not found, try to find any java executable
    if ! command -v java &> /dev/null; then
        JAVA_PATH=$(find /usr /opt -name java -type f -executable 2>/dev/null | head -1)
        if [ -n "$JAVA_PATH" ]; then
            export JAVA_HOME=$(dirname $(dirname "$JAVA_PATH"))
            export PATH=$JAVA_HOME/bin:$PATH
            echo "Found Java at: $JAVA_HOME"
        fi
    fi
fi

# Verify Java installation
echo "Verifying Java installation..."
if command -v java &> /dev/null; then
    java -version
else
    echo "ERROR: Java is still not found!"
    echo "Listing available JVM installations:"
    ls -la /usr/lib/jvm/ 2>/dev/null || echo "No JVM directory found"
    exit 1
fi

# Install Maven
echo "Installing Maven..."
MAVEN_VERSION=3.9.6
wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz
export PATH=$PWD/apache-maven-${MAVEN_VERSION}/bin:$PATH

# Verify Maven installation
echo "Maven version:"
mvn --version

# Navigate to backend directory
cd backend

# Build the application
echo "Running Maven build..."
mvn clean package -DskipTests

echo "Build completed successfully!"
