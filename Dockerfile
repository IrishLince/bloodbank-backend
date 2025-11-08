# Use Amazon Corretto 17 as base image (reliable for Railway deployment)
FROM amazoncorretto:17-alpine

# Install required packages for Maven
RUN apk add --no-cache bash maven

# Set working directory
WORKDIR /app

# Copy Maven wrapper files
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./

# Make Maven wrapper executable
RUN chmod +x mvnw

# Copy pom.xml
COPY pom.xml .

# Download dependencies (cache layer)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application using Maven directly (more reliable than wrapper)
RUN mvn clean package -DskipTests -B

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
CMD ["java", "-jar", "target/app-0.0.1-SNAPSHOT.jar"]