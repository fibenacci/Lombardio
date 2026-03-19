ARG DEV_BASE_IMAGE=maven:3.9.9-eclipse-temurin-17
ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-17
FROM ${DEV_BASE_IMAGE} AS development

WORKDIR /workspace
EXPOSE 8081
CMD ["mvn", "-q", "-DskipTests", "spring-boot:run"]

FROM ${BUILD_BASE_IMAGE} AS build

WORKDIR /build
COPY services/identity-access/pom.xml ./pom.xml
COPY services/identity-access/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app
COPY --from=build /build/target/identity-access-0.0.1-SNAPSHOT.jar /app/identity-access.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/identity-access.jar"]
