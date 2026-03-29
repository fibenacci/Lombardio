ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8084
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY pom.xml ./pom.xml
COPY services/platform-security ./services/platform-security
COPY services/identity-intelligence ./services/identity-intelligence
RUN mvn -q -DskipTests package -pl services/identity-intelligence -am

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app
COPY --from=build /build/services/identity-intelligence/target/identity-intelligence-0.0.1-SNAPSHOT.jar /app/identity-intelligence.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/identity-intelligence.jar"]
