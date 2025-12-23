# Build stage: use Maven to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage: use a lightweight JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/whatsapp-lld-1.0.0.jar /app/
COPY --from=build /app/target/dependency /app/dependency
EXPOSE 8080
EXPOSE 8081
CMD ["java", "-cp", "whatsapp-lld-1.0.0.jar:dependency/*", "com.whatsapp.RestServer"]
