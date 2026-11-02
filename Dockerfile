FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the pre-built jar (built locally via ./gradlew bootJar)
COPY build/libs/*.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]