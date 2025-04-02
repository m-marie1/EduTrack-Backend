# Stage 1: Build the application using Maven
FROM maven:3.8-eclipse-temurin-17-alpine AS builder
WORKDIR /app
# Copy pom.xml and download dependencies first to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy the rest of the source code
COPY src ./src
# Build the application JAR
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Set the entrypoint to run the application
# We use exec form to properly handle signals
# Added JAVA_OPTS for potential memory tuning in production
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 