# Auditoría e implementación

## Estado encontrado

El repositorio contenía Kotlin/Ktor, Exposed v1, modelos y repositorios del marketplace, migrations V1–V5 y autenticación avanzada. La primera ejecución detectó 46 tests y 15 fallos: seis de StatusPages y nueve por Docker apagado. El JDK 21 estaba instalado pero no configurado en el entorno de la sesión.

Se encontraron validación sin traducción HTTP, import incorrecto de NotFoundException, aserciones contradictorias de StatusPages, tests suspend/no-Unit que JUnit no descubría, un test de login que no invocaba LoginUseCase, permisos duplicados, ausencia de CORS/rate limit/request ID, un TODO en la dirección por defecto, variables DB de Compose inconsistentes con application.conf, configuración YAML obsoleta y Dockerfile sin comando de arranque.

## Fase 4F

- BCrypt: hash, salt, verificación positiva/negativa.
- Generador de refresh: aleatoriedad y hash SHA-256.
- Verificador JWT real: firma, subject, issuer, audience, expiración y roles; HTTP 401 para tokens incorrectos o incompletos.
- Validación: registro/login, normalización de identificadores y límite BCrypt de 72 bytes.
- StatusPages: 400/401/403/404/409/429/500 seguro, sin detalles de SQL ni credenciales en respuestas.
- Auth E2E real: Testcontainers → Flyway → Exposed → repositorios → casos de uso → Koin → configuración Ktor compartida → HTTP.
- Rotación concurrente completa: exactamente un éxito y un AuthenticationException, con ambos consumidores sincronizados antes de revocar.
- Rollback de rotación: se revierten revocación e inserción si falla la sesión nueva.
- JSON estricto e inyección ADMIN rechazada; login sin enumeración por respuesta; cuenta inactiva rechazada.
- Rate limiting, logout y revocación por usuario.

## Marketplace y REST

Casos de uso de direcciones, restaurantes, categorías, productos, drivers, administración y pedidos. Permisos centralizados en domain.security; Application verifica ownership y roles vigentes de PostgreSQL. DTOs de presentación separados del dominio.

Creación de pedidos con precios del servidor, snapshots de nombre/precio, enteros Money con overflow comprobado, historial y escritura atómica. Claves idempotentes por cliente para reintentos de creación. Transiciones centralizadas y timestamps. Locks de filas y una restricción DB protegen aceptación concurrente y disponibilidad del driver. La suite prueba también dos pedidos compitiendo por un solo driver.

Paginación y filtros de catálogo, historial y recuperación mediante REST; efectivo como único método de pago. WebSockets se documenta como extensión opcional.

## Schema

- V1: tablas iniciales y relaciones.
- V2: una dirección predeterminada por usuario.
- V3: constraints de driver.
- V4: constraints de estado y dinero de pedidos.
- V5: refresh tokens almacenados por hash.
- V6: idempotencia de pedidos y un único pedido activo por driver.

V1–V5 se preservaron. Las pruebas aplican todas las migrations sobre PostgreSQL vacío y comprueban rollback, constraints y concurrencia. No se ejecutaron truncados ni migrations contra la base de desarrollo.

## Producción

Configuración de entorno obligatoria para secretos, Docker multi-stage/usuario sin privilegios, Compose con DB privada, health/readiness, Caddy/HTTPS y procedimiento de backups/primer administrador. OpenAPI describe 47 operaciones y 27 schemas y se valida con el parser OpenAPI y pruebas HTTP de Swagger.

## Validación final

Consultar el resultado actualizado en `docs/VERIFICATION.md`. La implementación no implica un despliegue en un VPS real. Deben configurarse secretos, dominio, backups y tarifa antes del lanzamiento.
