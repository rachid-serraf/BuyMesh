# Stage 1: Build the application
FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (skipping tests for speed)
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:11-jre
WORKDIR /app

RUN apk add --no-cache curl

# Copy ONLY the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
