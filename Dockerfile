# syntax=docker/dockerfile:1

# ---- Build stage: compiles the shaded runnable jar with Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
# Tests are skipped here because they shell out to real ffmpeg/gs binaries,
# which aren't installed in this build stage. CI (.github/workflows/maven.yml)
# runs the full test suite with both binaries present.
RUN mvn -B package -DskipTests

# ---- Runtime stage: JRE + the ffmpeg/gs binaries the app shells out to ----
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg ghostscript \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/app
COPY --from=build /build/target/cloud-drive-compressor-1.0.0-runner.jar /opt/app/app.jar

# All app-relative outputs (OAuth tokens/, google-drive-files.csv, downloaded
# media) are written under the working directory, so mount a host folder here.
WORKDIR /data

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
