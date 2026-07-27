# syntax=docker/dockerfile:1
ARG SERVICE=gateway
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :${SERVICE}:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine
ARG SERVICE=gateway
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
