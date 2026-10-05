FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src src
RUN chmod +x gradlew && ./gradlew --no-daemon buildFatJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S -G app -u 1001 app
COPY --from=builder --chown=app:app /app/build/libs/*-all.jar /app/app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 CMD wget -q -O - http://127.0.0.1:8080/live || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
