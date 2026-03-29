ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8087
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY pom.xml ./pom.xml
COPY services/platform-security ./services/platform-security
COPY services/online-auction ./services/online-auction
RUN mvn -q -DskipTests package -pl services/online-auction -am

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app
COPY --from=build /build/services/online-auction/target/online-auction-service-0.0.1-SNAPSHOT.jar /app/online-auction-service.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "/app/online-auction-service.jar"]
