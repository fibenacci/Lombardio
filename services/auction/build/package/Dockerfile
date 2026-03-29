ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8086
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY pom.xml ./pom.xml
COPY services/platform-security ./services/platform-security
COPY services/auction ./services/auction
RUN mvn -q -DskipTests package -pl services/auction -am

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app
COPY --from=build /build/services/auction/target/auction-service-0.0.1-SNAPSHOT.jar /app/auction-service.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/auction-service.jar"]
