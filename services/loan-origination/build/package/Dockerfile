ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8083
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY pom.xml ./pom.xml
COPY services/platform-security ./services/platform-security
COPY services/loan-origination ./services/loan-origination
RUN mvn -q -DskipTests package -pl services/loan-origination -am

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app
COPY --from=build /build/services/loan-origination/target/loan-origination-0.0.1-SNAPSHOT.jar /app/loan-origination.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/loan-origination.jar"]
