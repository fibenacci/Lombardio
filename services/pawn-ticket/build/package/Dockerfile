ARG DEV_BASE_IMAGE=lombardio-identity-access:latest
ARG BUILD_BASE_IMAGE=lombardio-identity-access:latest
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8085
CMD ["mvn", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY services/pawn-ticket/pom.xml ./pom.xml
COPY services/pawn-ticket/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app
COPY --from=build /build/target/pawn-ticket-service-0.0.1-SNAPSHOT.jar /app/pawn-ticket-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/pawn-ticket-service.jar"]
