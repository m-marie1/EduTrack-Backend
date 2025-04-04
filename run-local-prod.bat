@echo off
REM Run the Spring Boot application in production mode locally

REM Set environment variables for production mode
set SPRING_PROFILES_ACTIVE=prod
set DB_URL=jdbc:postgresql://localhost:5432/attendance_db
set DB_USERNAME=postgres
set DB_PASSWORD=postgres

REM Email configuration
set MAIL_HOST=smtp-relay.brevo.com
set MAIL_PORT=587
set MAIL_USERNAME=896450001@smtp-brevo.com
set MAIL_PASSWORD=KkOVE863CUbY5PM1
set MAIL_FROM=outsidethebox3310@gmail.com

REM JWT configuration - generate a stronger secret for production
set JWT_SECRET=local_production_test_jwt_secret_key_that_should_be_very_long_and_secure_for_proper_testing
set JWT_EXPIRATION_MS=86400000

REM Start the application
mvn spring-boot:run

REM If you want to build and run the JAR instead, uncomment these lines:
REM mvn clean package -DskipTests
REM java -jar target/attendance-system-0.0.1-SNAPSHOT.jar 