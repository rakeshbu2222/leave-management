# Stage 1: Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration first for dependency caching
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build the Spring Boot JAR
RUN mvn clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create a non-root user
RUN useradd --create-home --shell /bin/bash appuser

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run the application as non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]