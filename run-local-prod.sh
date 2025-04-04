#!/bin/bash
# Run the Spring Boot application in production mode locally

# Set environment variables for production mode
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/attendance_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# Email configuration
export MAIL_HOST=smtp-relay.brevo.com
export MAIL_PORT=587
export MAIL_USERNAME=896450001@smtp-brevo.com
export MAIL_PASSWORD=KkOVE863CUbY5PM1
export MAIL_FROM=outsidethebox3310@gmail.com

# JWT configuration - generate a stronger secret for production
export JWT_SECRET=local_production_test_jwt_secret_key_that_should_be_very_long_and_secure_for_proper_testing
export JWT_EXPIRATION_MS=86400000

# Start the application
./mvnw spring-boot:run

# If you want to build and run the JAR instead, uncomment these lines:
# ./mvnw clean package -DskipTests
# java -jar target/attendance-system-0.0.1-SNAPSHOT.jar 