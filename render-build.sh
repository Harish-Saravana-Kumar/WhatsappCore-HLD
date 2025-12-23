#!/bin/bash
set -e

echo "Building WhatsApp Backend for Render..."

# Install Maven
MAVEN_VERSION=3.9.6
wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz
export PATH=$PWD/apache-maven-${MAVEN_VERSION}/bin:$PATH

# Verify Maven installation
mvn --version

# Build the application
cd backend
mvn clean package -DskipTests

echo "Build completed successfully!"
