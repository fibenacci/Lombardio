ARG DEV_BASE_IMAGE=lombardio-identity-access:latest
ARG BUILD_BASE_IMAGE=lombardio-identity-access:latest
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8091
CMD ["mvn", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY services/reporting/pom.xml ./pom.xml
COPY services/reporting/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app
COPY --from=build /build/target/reporting-service-0.0.1-SNAPSHOT.jar /app/reporting-service.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app/reporting-service.jar"]
