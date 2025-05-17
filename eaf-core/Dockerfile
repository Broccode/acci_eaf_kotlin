# Build stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY ./gradlew ./gradlew
COPY ./gradlew.bat ./gradlew.bat
COPY ./gradle ./gradle
COPY ./eaf-core ./eaf-core
COPY ./eaf-multitenancy ./eaf-multitenancy
COPY ./build-logic ./build-logic
COPY ./build.gradle.kts ./build.gradle.kts
COPY ./settings.gradle.kts ./settings.gradle.kts
RUN ./gradlew :eaf-core:build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy built artifacts from the build stage
COPY --from=build /app/eaf-core/build/libs/*.jar /app/eaf-core.jar

# Ownership and permissions
RUN chown -R appuser:appgroup /app
USER appuser

# Configure application runtime
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
EXPOSE 8080

# Run the application with proper options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/eaf-core.jar"] 