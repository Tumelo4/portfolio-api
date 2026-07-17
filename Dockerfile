FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp clean package


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

COPY --from=build \
    /workspace/target/portfolio-api.jar \
    /app/portfolio-api.jar

USER spring:spring

EXPOSE 10000

ENTRYPOINT [
    "java",
    "-XX:MaxRAMPercentage=75.0",
    "-jar",
    "/app/portfolio-api.jar"
]