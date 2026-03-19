ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-17
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-17
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8083
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY services/loan-origination/pom.xml ./pom.xml
COPY services/loan-origination/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app
COPY --from=build /build/target/loan-origination-0.0.1-SNAPSHOT.jar /app/loan-origination.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/loan-origination.jar"]
