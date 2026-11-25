# Stage 1: Build the application
FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:11-jre
WORKDIR /app

# Install openssl and curl on Debian/Ubuntu
RUN apt-get update && apt-get install -y --no-install-recommends \
    openssl \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "openssl req -x509 -newkey rsa:4096 -keyout /tmp/key.pem -out /tmp/cert.pem -sha256 -days 365 -nodes -subj '/C=MA/ST=Oujda/L=Oujda/O=Zone01/OU=IT/CN=localhost' && openssl pkcs12 -export -in /tmp/cert.pem -inkey /tmp/key.pem -out /app/edge-service.p12 -name springgateway -passout pass:${SSL_PASSWORD} && rm /tmp/key.pem /tmp/cert.pem && exec java -jar app.jar --server.ssl.key-store=file:/app/edge-service.p12"]