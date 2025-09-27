# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Set MongoDB connection timeout and retry options
ENV SPRING_DATA_MONGODB_CONNECT_TIMEOUT=10000
ENV SPRING_DATA_MONGODB_SOCKET_TIMEOUT=10000
ENV SPRING_DATA_MONGODB_MAX_WAIT_TIME=10000
ENV SPRING_DATA_MONGODB_SERVER_SELECTION_TIMEOUT=10000

# Run the application
CMD ["java", "-Dspring.profiles.active=production", "-jar", "target/app-0.0.1-SNAPSHOT.jar"]