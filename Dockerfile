# ==========================================
# BUILD
# ==========================================

FROM gradle:9.3-jdk21 AS builder

WORKDIR /app

COPY . .

RUN gradle clean buildFatJar --no-daemon


# ==========================================
# RUNTIME
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd \
    --system \
    --create-home \
    --uid 1001 \
    appuser

COPY --from=builder \
    /app/build/libs/*-all.jar \
    /app/app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

