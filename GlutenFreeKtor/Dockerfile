# Stage 1: Build the application
FROM gradle:8.12.0-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /Users/ivan.sanchez/Documents/FreeGluApp/GlutenFreeKtor
# Copy source over and run build
WORKDIR /home/gradle/src
RUN ./gradlew installDist --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
EXPOSE 8081

# Create a secure non-root user and group
RUN addgroup --system ktor && adduser --system --ingroup ktor ktor

WORKDIR /app
COPY --from=build --chown=ktor:ktor /home/gradle/src/build/install/GlutenFreeKtor /app

USER ktor

# Run the Ktor start script
ENTRYPOINT ["/app/bin/GlutenFreeKtor"]
