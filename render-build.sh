#!/bin/bash
set -e

echo "Building WhatsApp Backend for Render..."

# Set JAVA_HOME (Render provides Java)
export JAVA_HOME=/opt/render/project/.render/java/jdk-17.0.1
export PATH=$JAVA_HOME/bin:$PATH

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
