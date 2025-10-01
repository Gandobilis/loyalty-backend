# Stage 1: Build the application using Maven with JDK 25
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy the pom.xml and download dependencies first to leverage Docker's build cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests for a faster build
RUN mvn package -DskipTests

# Stage 2: Create the final, lightweight runtime image using JRE 25
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

# Copy the JAR file from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]