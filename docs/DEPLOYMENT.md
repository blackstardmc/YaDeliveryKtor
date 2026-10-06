# Despliegue en VPS

## Preparación

Usar Docker Engine con Compose v2 en Linux y un dominio que resuelva al VPS. Abrir solamente SSH, TCP 80/443 y opcionalmente UDP 443. PostgreSQL permanece en la red interna de Compose. El backend publica 8080 únicamente en loopback.

1. Copiar el proyecto y `.env.example` a `.env`.
2. Establecer un `DB_PASSWORD` único y un `JWT_SECRET` aleatorio de al menos 32 bytes. Por ejemplo, generar dos valores independientes con `openssl rand -hex 32` y guardarlos en `.env` con permisos 600.
3. Establecer `API_DOMAIN=api.tudominio.com`, `CORS_ALLOWED_HOSTS=app.tudominio.com` y la tarifa `DELIVERY_FEE_CENTS` en centavos. Los hosts CORS se escriben sin esquema ni ruta; separar varios con comas.
4. Conservar `DB_NAME`/`DB_USER` y contraseña que realmente correspondan al volumen existente. Compose conserva el volumen `delivery-postgres-data`. Las variables de PostgreSQL solamente inicializan un volumen vacío.
5. Ejecutar:

```sh
docker compose -f compose.yaml -f compose.prod.yaml config --quiet
docker compose -f compose.yaml -f compose.prod.yaml up -d --build
curl --fail https://api.tudominio.com/ready
```

Caddy obtiene y renueva HTTPS automáticamente. El overlay activa `TRUST_PROXY=true`; Caddy reemplaza `X-Forwarded-For` con la IP de su conexión. Mantener el backend inaccesible directamente desde Internet. No activar esa opción en un backend público sin proxy confiable.

El Dockerfile utiliza Java 21 en dos etapas, construye con el Gradle Wrapper del repositorio y ejecuta el jar como usuario sin privilegios. `.dockerignore` excluye `.env`, credenciales locales, Git y outputs. La imagen no incorpora secretos. Las migrations se ejecutan mediante Flyway al arrancar; un fallo impide iniciar el servicio.

El healthcheck del contenedor consulta `/live`; el monitoreo o balanceador debe consultar `/ready`. Un PostgreSQL caído produce 503 sin detalles internos y no convierte liveness en una prueba de DB. Caddy limita bodies a 1 MB. El rate limit del MVP es local a cada instancia; usar una única instancia hasta incorporar un límite compartido o equivalente en el proxy.

## Primera cuenta administradora

1. Registrar la cuenta operadora mediante `/auth/register` y confirmar su identidad. El endpoint solamente crea CUSTOMER.
2. Abrir una sesión de administración de la base desde el host:

```sh
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

3. En `psql`, reemplazar el correo del ejemplo por la cuenta operadora verificada:

```sql
\set admin_email 'operador@example.com'
BEGIN;
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users
WHERE email = :'admin_email' AND is_active = TRUE
ON CONFLICT DO NOTHING;
COMMIT;
```

Confirmar que se insertó una fila. Iniciar sesión otra vez para obtener un JWT con el nuevo rol. Desde entonces, `PUT /admin/users/{id}` administra roles y estado. Al asignar DRIVER se crea su perfil OFFLINE; el conductor puede pasar a AVAILABLE. La creación de un restaurante requiere rol RESTAURANT o ADMIN y una dirección propia. Un restaurante nuevo comienza INACTIVE.

La operación anterior modifica datos administrativos, no el schema. Nunca usar SchemaUtils ni editar migrations aplicadas.

## Backups y actualizaciones

Guardar backups fuera del VPS, cifrados y con retención. Ejemplo Linux para un dump consistente:

```sh
docker compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > delivery.dump
```

Probar la restauración en una base vacía aislada. Ejemplo dentro de un entorno de recuperación preparado con su propio volumen:

```sh
cat delivery.dump | docker compose exec -T postgres sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --exit-on-error'
```

Antes de actualizar: ejecutar tests, generar backup, revisar migrations nuevas y construir la imagen. Aplicar `docker compose ... up -d --build`, comprobar readiness y recorrer login/pedidos. Conservar la imagen anterior para rollback de aplicación; las migrations son hacia adelante y pueden requerir restauración del backup para revertir schema. No ejecutar `docker compose down -v` sobre datos persistentes.

V6 agrega claves de idempotencia y un índice único de pedidos activos por driver. Antes de aplicarla a una base con datos existentes, comprobar que ningún driver tenga varios pedidos READY/IN_DELIVERY asignados. Resolver inconsistencias operativas antes de migrar; Flyway no las borra automáticamente.

```sql
SELECT driver_id, count(*)
FROM orders
WHERE driver_id IS NOT NULL AND status IN ('READY', 'IN_DELIVERY')
GROUP BY driver_id HAVING count(*) > 1;
```

## Prueba de empaquetado

```sh
docker build -t delivery-backend:mvp .
pwsh scripts/smoke.ps1
```

El script crea PostgreSQL efímero, una red propia y un backend en puerto loopback aleatorio. Comprueba migrations V1–V6, OpenAPI/Swagger, register/login/me/refresh/replay/logout y readiness durante una caída de DB; después elimina solamente esos recursos. No utiliza ni limpia el volumen de desarrollo.

Si Docker Hub no está disponible, se puede probar únicamente el jar en una imagen Java 21 local:

```powershell
.\scripts\smoke.ps1 -RuntimeImage <imagen-java21-local> -JarPath .\build\libs\delieverybackend-all.jar
```

Esta alternativa verifica el jar y el bootstrap real; no sustituye una construcción completa del Dockerfile. La validación HTTPS requiere el dominio real y se realiza en el VPS; este trabajo no despliega ni cambia DNS.

Para comprobar el conjunto base de Compose después de construir la imagen:

```powershell
pwsh scripts/smoke-compose.ps1
```

Esta prueba utiliza `compose.yaml` con un overlay temporal que selecciona la imagen local y reemplaza el volumen por uno exclusivo de la ejecución. No carga `.env`; genera secretos temporales y usa un puerto aleatorio en loopback. Comprueba healthchecks, migraciones, registro, persistencia tras reiniciar el backend y readiness durante una caída de PostgreSQL. Elimina su proyecto y volumen al finalizar. No incluye Caddy ni verifica HTTPS público.

## Extensión de tiempo real

REST permite recuperar pedidos e historial después de una desconexión. WebSockets queda fuera del MVP estable. Una implementación futura debe autenticar cada conexión, autorizar cada suscripción por pedido y permitir resuscripción usando el estado REST. No usar eventos en memoria como fuente definitiva de estado ni enviar pedidos ajenos a un cliente, restaurante o driver.
