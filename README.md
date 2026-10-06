# Delivery backend

MVP REST para delivery con pagos en efectivo, Kotlin 2.4.0, Ktor 3.5.1, Exposed 1.3.1, PostgreSQL 17, Flyway 13, Koin 4.2.1 y Java 21. Las versiones exactas están en `build.gradle.kts` y los catálogos de Gradle. La suite usa JUnit Jupiter 6 y Testcontainers 1.21.4.

## Ejecutar los tests

Requisitos: JDK 21 y Docker con contenedores Linux activos. No se necesita `.env` para los tests. Todos los tests de persistencia crean PostgreSQL aislado; no usan la base de desarrollo.

```powershell
.\gradlew.bat clean test buildFatJar
```

En Linux: `./gradlew clean test buildFatJar`. Informe: `build/reports/tests/test/index.html`.

## Arranque local con Docker

1. Copiar `.env.example` a `.env` y completar `DB_PASSWORD` y `JWT_SECRET` (al menos 32 bytes aleatorios). Conservar los valores correctos del volumen existente; cambiar variables no cambia las credenciales de un PostgreSQL ya inicializado.
2. Ejecutar `docker compose up -d --build`.
3. Abrir `http://localhost:8080/swagger`. Contrato: `http://localhost:8080/openapi.json`.

El backend se publica solamente en loopback. PostgreSQL no publica puertos. Para desarrollar ejecutando Gradle en el host, iniciar únicamente PostgreSQL con `docker compose -f compose.yaml -f compose.dev.yaml up -d postgres`, exportar las variables de `.env` al proceso y ejecutar `./gradlew run`. El overlay de desarrollo publica PostgreSQL solamente en `127.0.0.1:5432`. Ktor no carga `.env` automáticamente.

## API y reglas

- Registro público siempre crea CUSTOMER. Los roles RESTAURANT/DRIVER/ADMIN se asignan administrativamente.
- Auth: `/auth/register`, `/auth/login`, `/auth/me`, `/auth/refresh`, `/auth/logout`, `/auth/logout-all`. `/users/me` es un alias del perfil.
- Direcciones propias; restaurantes y catálogo público paginado; gestión de catálogo por propietario; pedidos de cliente/restaurante/driver; administración de usuarios y estados.
- `RECEIVED → CONFIRMED → PREPARING → READY → IN_DELIVERY → DELIVERED`. La aceptación del driver realiza READY → IN_DELIVERY. Cancelación solamente desde RECEIVED, CONFIRMED o PREPARING.
- Todos los importes son enteros en centavos de una única moneda operativa del despliegue. `DELIVERY_FEE_CENTS` define la tarifa fija; por defecto 0. El cliente no decide precios ni totales.
- Crear pedidos con `Idempotency-Key` y reutilizar esa misma clave y payload en reintentos. El servidor devuelve el mismo pedido con su estado actual. Cambiar el payload con la misma clave devuelve 409.
- El detalle del pedido incluye historial, direcciones de recogida/entrega y teléfono del cliente solamente para actores autorizados. Las direcciones utilizadas por pedidos o restaurantes no pueden editarse; crear otra dirección permite conservar los datos anteriores.
- Paginación: `limit=20` por defecto (máximo 100), `offset=0` (máximo 100000). Los listados devuelven arrays. Productos admiten `categoryId`, `available` y `search`.
- JSON estricto: campos desconocidos devuelven 400. Errores: `{ "error": "...", "message": "...", "field": null }`.
- `/health` y `/live`: proceso vivo. `/ready`: consulta PostgreSQL y devuelve 503 si no está disponible.

## Sesiones y conectividad

Los refresh tokens se guardan como SHA-256, rotan y se consumen atómicamente. Serializar el refresh en el cliente: reutilizar A tras recibir B devuelve 401, pero B sigue válido. Si la respuesta de una rotación se pierde y ya no se conserva un token válido, volver a iniciar sesión. Logout revoca refresh tokens; el JWT de acceso tiene vida corta y expira según configuración. La autorización del marketplace consulta los roles y el estado actual de la cuenta en PostgreSQL.

Auth limita 20 mutaciones por minuto por IP y proceso, con `Retry-After` al recibir 429. El despliegue MVP es de una instancia; antes de escalar horizontalmente se necesita un límite compartido o en el proxy. Para reconectar, consultar `GET /orders/{id}` y los listados REST con backoff. WebSockets queda como extensión opcional; REST es la fuente de verdad.

## Documentación y despliegue

- [Despliegue, administración inicial y backups](docs/DEPLOYMENT.md).
- [Auditoría, fases y validación](docs/IMPLEMENTATION.md).
- OpenAPI se genera a partir de DTOs y metadatos explícitos de rutas: `python scripts/generate_openapi.py`. Verificar con `python scripts/generate_openapi.py --check`.
- Smoke de la imagen final: `pwsh scripts/smoke.ps1`. Usa recursos efímeros propios y los elimina al terminar.
- Smoke de Compose: `pwsh scripts/smoke-compose.ps1`. Usa la imagen local `delivery-backend:mvp`, un volumen propio y un puerto temporal; comprueba salud, reinicio y caída de PostgreSQL.
