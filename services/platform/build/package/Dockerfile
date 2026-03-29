ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8082
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY pom.xml ./pom.xml
COPY services/platform-security ./services/platform-security
COPY services/platform ./services/platform
RUN mvn -q -DskipTests package -pl services/platform -am

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app
COPY --from=build /build/services/platform/target/platform-0.0.1-SNAPSHOT.jar /app/platform.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/platform.jar"]
