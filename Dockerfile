# Multi-stage build: compile with Maven+JDK21, run on a slim JRE21.
# Defaults to the "h2" Spring profile (in-memory DB, seeded on boot) so this
# image needs no external database at all — good enough for a demo deploy.
# For a real deployment, set SPRING_PROFILES_ACTIVE=mysql and the DB_* env vars
# documented in README.md.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/hadiid-erp.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
