# Unified Dockerfile for all Java services in the Monorepo
# Built from the root to ensure correct dependency resolution

ARG BUILD_BASE_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG RUNTIME_BASE_IMAGE=eclipse-temurin:21-jre

# --- Build Stage ---
FROM ${BUILD_BASE_IMAGE} AS build
ARG SERVICE_NAME
WORKDIR /build

# 1. Copy everything needed for the build
# In a monorepo, we need the parent pom and ALL modules to resolve dependencies correctly
COPY pom.xml ./
COPY spotbugs-exclude.xml ./
COPY services/platform-security ./services/platform-security
COPY services/${SERVICE_NAME} ./services/${SERVICE_NAME}
COPY spotbugs-exclude.xml ./services/platform-security/spotbugs-exclude.xml
COPY spotbugs-exclude.xml ./services/${SERVICE_NAME}/spotbugs-exclude.xml

# 2. Build using cache mount
# First install the parent pom (non-recursive)
RUN --mount=type=cache,target=/root/.m2 \
    mvn install -N -DskipTests

# Then install the shared security module
RUN --mount=type=cache,target=/root/.m2 \
    mvn install -f services/platform-security/pom.xml -DskipTests

# Finally build the target service
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -f services/${SERVICE_NAME}/pom.xml -DskipTests

# --- Runtime Stage ---
FROM ${RUNTIME_BASE_IMAGE} AS runtime
ARG SERVICE_NAME
ARG SERVICE_PORT=8080
WORKDIR /app

# Copy the built jar using a wildcard that matches the service name
# This avoids picking up plugins (like findsecbugs) or the 'original-' jar
COPY --from=build /build/services/${SERVICE_NAME}/target/${SERVICE_NAME}*.jar app.jar

EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
