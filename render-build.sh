#!/bin/bash
set -e

echo "Building WhatsApp Backend for Render..."

# Find and set JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    echo "Detecting Java installation..."
    # Try common locations
    if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    elif [ -d "/usr/lib/jvm/java-11-openjdk-amd64" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
    elif [ -d "/opt/java/openjdk" ]; then
        export JAVA_HOME=/opt/java/openjdk
    else
        # Try to find java executable
        JAVA_PATH=$(which java 2>/dev/null || find /usr /opt -name java -type f 2>/dev/null | head -1)
        if [ -n "$JAVA_PATH" ]; then
            export JAVA_HOME=$(dirname $(dirname $JAVA_PATH))
        fi
    fi
    
    if [ -n "$JAVA_HOME" ]; then
        export PATH=$JAVA_HOME/bin:$PATH
        echo "JAVA_HOME set to: $JAVA_HOME"
    fi
fi

# Verify Java installation
echo "Java version:"
java -version

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
