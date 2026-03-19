ARG DEV_BASE_IMAGE=lombardio-identity-access:latest
ARG BUILD_BASE_IMAGE=lombardio-identity-access:latest
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8082
CMD ["mvn", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY services/platform/pom.xml ./pom.xml
COPY services/platform/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app
COPY --from=build /build/target/platform-0.0.1-SNAPSHOT.jar /app/platform.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/platform.jar"]
