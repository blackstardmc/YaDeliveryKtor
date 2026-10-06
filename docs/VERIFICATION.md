# Verificación del MVP

Validación inicial: 4 de octubre de 2026, America/Havana. Imagen Docker verificada el 5 de octubre; Compose base verificado el 6 de octubre de 2026.

## Resultado

- `clean test buildFatJar`: BUILD SUCCESSFUL con Java 21. La suite ejecutada produjo 79 tests en 28 clases, cero fallos, cero errores y cero omitidos. La reconstrucción posterior al ajuste de Shadow reutilizó esos resultados desde la caché de Gradle.
- PostgreSQL real mediante Testcontainers: migrations, constraints, rollback, refresh concurrente, ownership, flujo de pedidos y aceptación concurrente de drivers comprobados.
- `scripts/generate_openapi.py --check`: 47 operaciones y 27 schemas; contrato validado también con OpenAPIV3Parser y pruebas HTTP.
- `docker compose -f compose.yaml -f compose.prod.yaml config --quiet`: correcto, usando valores de validación sin iniciar servicios.
- `git diff --check`: correcto con el tratamiento CRLF de Windows.
- Smoke del jar en Docker: **SMOKE PASSED**.
- Construcción completa del Dockerfile e integración con la imagen final `delivery-backend:mvp`: **BUILD SUCCESSFUL / SMOKE PASSED** (5 de octubre).

## Artefacto ejecutable

```powershell
$env:JAVA_HOME='C:\Users\Work\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2'
$env:GRADLE_USER_HOME='C:\Users\Work\.gradle'
.\gradlew.bat clean test buildFatJar
.\scripts\smoke.ps1 -RuntimeImage delieverybackend-backend:latest -JarPath .\build\libs\delieverybackend-all.jar -Docker 'C:\Program Files\Docker\Docker\resources\bin\docker.exe'
```

La imagen local proporcionó Java 21.0.11 y ejecutó el jar recién construido mediante un montaje de solo lectura. La prueba creó una red y PostgreSQL temporales, sin utilizar el volumen de desarrollo, y verificó:

1. Arranque real de EngineMain, Koin, Hikari y Flyway V1–V6 sobre una base vacía.
2. `/ready`, `/live`, `/openapi.json` y `/swagger`.
3. Registro CUSTOMER, login, identidad autenticada, rotación, rechazo de replay, logout y rechazo del token revocado.
4. PostgreSQL detenido: readiness 503 y liveness 200.
5. Eliminación de los contenedores y la red propios de la prueba.

Esta comprobación detectó y permitió corregir un problema de empaquetado: Shadow descartaba registros ServiceLoader duplicados. Ahora se fusionan los registros usados por los lectores de configuración de Ktor y los plugins de Flyway. El smoke posterior pasó.

## Imagen Docker final

Tras habilitar el acceso en el cortafuegos, la construcción completa terminó correctamente. Docker descargó las imágenes base y Gradle, compiló el proyecto dentro de Linux y generó la imagen final. El bloqueo anterior de acceso a Docker Hub quedó resuelto.

Comandos verificados:

```powershell
docker build --progress=plain -t delivery-backend:mvp .
pwsh scripts/smoke.ps1 -RuntimeImage delivery-backend:mvp
```

Imagen: `sha256:cf4af55fe57db6a4c9503d2aabe063954e8c582797f8e02c7a637d7040c803e7`. Usuario configurado: `app`; entrypoint: `java -jar /app/app.jar`.

El smoke ejecutó el contenido de esta imagen sin montar un jar externo y pasó todas las comprobaciones enumeradas arriba. Los contenedores y la red temporales se eliminaron al finalizar; la imagen permanece disponible localmente.

## Alcance pendiente

El 6 de octubre se ejecutó `scripts/smoke-compose.ps1` con resultado **COMPOSE SMOKE PASSED**. El conjunto de `compose.yaml` arrancó con ambos servicios saludables; se comprobaron seis migraciones, registro, identidad persistida tras reiniciar el backend, readiness 503 al detener PostgreSQL y liveness 200. Se utilizó un proyecto exclusivo, volumen propio, secretos temporales y puerto loopback aleatorio. El script consulta nuevamente el puerto después del reinicio porque Docker puede reasignarlo.

No se realizó un despliegue real en VPS ni una comprobación de Caddy/HTTPS con dominio público. Los pasos están en [DEPLOYMENT.md](DEPLOYMENT.md). No se modificaron DNS, secretos locales ni datos de desarrollo.
