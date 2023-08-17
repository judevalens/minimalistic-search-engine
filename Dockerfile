# syntax=docker/dockerfile:1
FROM openjdk:22-slim-bullseye

WORKDIR /app

COPY target/MacayaSearch-1.0-SNAPSHOT.jar .

ENTRYPOINT ["java","-jar","MacayaSearch-1.0-SNAPSHOT.jar"]