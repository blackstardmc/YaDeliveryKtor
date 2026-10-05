"""Generate the API contract from the serializable DTOs and explicit route metadata.

Uses Python's standard library only. Run with --check in CI to detect stale output.
"""
import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/openapi/documentation.json"
ROLES = ["CUSTOMER", "RESTAURANT", "DRIVER", "ADMIN"]
ORDER_STATES = ["RECEIVED", "CONFIRMED", "PREPARING", "READY", "IN_DELIVERY", "DELIVERED", "CANCELLED"]


def ref(name):
    return {"$ref": f"#/components/schemas/{name}"}


def type_schema(kind, name=""):
    nullable = kind.endswith("?")
    kind = kind.rstrip("?")
    array = re.fullmatch(r"(List|Set)<(.+)>", kind)
    if array:
        result = {"type": "array", "items": type_schema(array[2])}
        if array[1] == "Set":
            result["uniqueItems"] = True
        if name == "roles":
            result["items"]["enum"] = ROLES
    elif kind == "String":
        result = {"type": "string"}
        if name == "id" or name.endswith("Id") or name == "changedBy":
            result["format"] = "uuid"
        elif name.endswith("At"):
            result["format"] = "date-time"
    elif kind in ("Int", "Long"):
        result = {"type": "integer", "format": "int64" if kind == "Long" else "int32"}
    elif kind == "Boolean":
        result = {"type": "boolean"}
    elif kind == "Double":
        result = {"type": "number", "format": "double"}
    else:
        result = ref(kind)
    if nullable:
        if "$ref" in result:
            result = {"allOf": [result]}
        result["nullable"] = True
    return result


def dto_schemas():
    presentation = ROOT / "src/main/kotlin/presentation"
    files = sorted((presentation / "auth/dto").glob("*.kt")) + [
        presentation / "marketplace/MarketplaceDtos.kt", presentation / "common/ErrorResponse.kt"]
    schemas = {}
    for file in files:
        source = file.read_text(encoding="utf-8-sig")
        for match in re.finditer(r"@Serializable\s+data class (\w+)\((.*?)\)", source, re.S):
            properties, required = {}, []
            for field in match[2].split(","):
                field = field.strip()
                if not field:
                    continue
                parsed = re.fullmatch(r"val\s+(\w+):\s*([\w<>?]+)(?:\s*=\s*(.+))?", field, re.S)
                if not parsed:
                    raise ValueError(f"Unsupported DTO field in {file}: {field}")
                name, kind, default = parsed.groups()
                properties[name] = type_schema(kind, name)
                if default is None:
                    required.append(name)
                elif match[1] != "AuthResponse" or name != "expiresIn":
                    properties[name]["default"] = json.loads(default)
            schemas[match[1]] = {"type": "object", "additionalProperties": False, "properties": properties}
            if required:
                schemas[match[1]]["required"] = required
    for name in ("RegisterRequest", "LoginRequest"):
        schemas[name]["properties"]["password"].update(writeOnly=True, description="Maximum 72 UTF-8 bytes; passwords are never logged or returned.")
    schemas["RegisterRequest"]["properties"]["password"]["minLength"] = 8
    for name in ("phone", "firstName", "lastName"):
        schemas["RegisterRequest"]["properties"][name].update(minLength=1, maxLength=20 if name == "phone" else 100)
    schemas["RegisterRequest"]["properties"]["email"].update(format="email", maxLength=255)
    schemas["AuthResponse"]["properties"]["expiresIn"]["description"] = "Access-token lifetime in seconds, from server configuration."
    schemas["OrderRequest"]["properties"]["items"].update(minItems=1, maxItems=50, description="Distinct product IDs from this restaurant. Prices come from the database.")
    schemas["OrderRequest"]["properties"]["notes"]["maxLength"] = 2000
    schemas["OrderLineRequest"]["properties"]["quantity"].update(minimum=1, maximum=100)
    schemas["ProductRequest"]["properties"]["priceCents"].update(minimum=1, maximum=1000000000000)
    schemas["OrderSummary"]["properties"]["status"]["enum"] = ORDER_STATES
    schemas["OrderSummary"]["properties"]["paymentMethod"]["enum"] = ["CASH"]
    for name in ("fromStatus", "toStatus"):
        schemas["OrderHistoryResponse"]["properties"][name]["enum"] = ORDER_STATES
    schemas["RestaurantResponse"]["properties"]["status"]["enum"] = ["ACTIVE", "INACTIVE", "SUSPENDED"]
    schemas["DriverResponse"]["properties"]["status"]["enum"] = ["OFFLINE", "AVAILABLE", "BUSY", "SUSPENDED"]
    schemas["DriverResponse"]["properties"]["vehicleType"]["enum"] = ["BICYCLE", "MOTORCYCLE", "CAR", "OTHER"]
    schemas["HealthResponse"] = {"type": "object", "required": ["status"], "properties": {"status": {"type": "string", "enum": ["UP", "DOWN"]}}}
    schemas["ApiInfo"] = {"type": "object", "required": ["name", "version", "status"], "properties": {key: {"type": "string"} for key in ("name", "version", "status")}}
    return schemas


def build():
    document = {"openapi": "3.0.3", "info": {"title": "Delivery API", "version": "1.0.0",
        "description": "Cash delivery marketplace. Money uses integer cents. JSON rejects unknown fields. List endpoints use limit (1–100, default 20) and offset (0–100000). JWT identifies the actor; marketplace permissions and ownership are checked against current database state. Auth mutations share 20 requests per minute per IP per backend instance."},
        "servers": [{"url": "/"}], "paths": {}, "components": {"securitySchemes": {"BearerAuth": {"type": "http", "scheme": "bearer", "bearerFormat": "JWT"}}, "schemas": dto_schemas()}}

    def endpoint(method, path, summary, response=None, request=None, *, public=False, status=200, paged=False, description="", rate=False):
        operation = {"operationId": method + re.sub(r"[^A-Za-z0-9]", "_", path), "summary": summary,
            "tags": [path.split("/")[1] or "health"], "security": [] if public else [{"BearerAuth": []}], "responses": {}}
        if description:
            operation["description"] = description
        params = [{"name": name, "in": "path", "required": True, "schema": {"type": "string", "format": "uuid"}} for name in re.findall(r"\{(\w+)\}", path)]
        if paged:
            params += [{"name": "limit", "in": "query", "schema": {"type": "integer", "minimum": 1, "maximum": 100, "default": 20}},
                       {"name": "offset", "in": "query", "schema": {"type": "integer", "minimum": 0, "maximum": 100000, "default": 0}}]
        if params:
            operation["parameters"] = params
        success = {"description": "Success", "headers": {"X-Request-ID": {"schema": {"type": "string", "format": "uuid"}, "description": "Server-generated correlation ID"}}}
        if response:
            schema = {"type": "array", "items": ref(response[2:])} if response.startswith("[]") else ref(response)
            success["content"] = {"application/json": {"schema": schema}}
        operation["responses"][str(status)] = success
        errors = [400, 404, 409, 500]
        if not public:
            errors += [401, 403]
        if path in ("/auth/login", "/auth/refresh"):
            errors += [401]
        if rate:
            errors += [429]
        descriptions = {400: "Invalid request", 401: "Authentication required or invalid credentials", 403: "Permission or ownership denied", 404: "Resource not found", 409: "Duplicate, invalid transition or conflicting state", 429: "Rate limit exceeded", 500: "Safe internal error"}
        for code in sorted(set(errors)):
            operation["responses"][str(code)] = {"description": descriptions[code], "content": {"application/json": {"schema": ref("ErrorResponse")}}}
        if rate:
            operation["responses"]["429"]["headers"] = {"Retry-After": {"schema": {"type": "integer"}, "description": "Seconds until another attempt"}}
        if request:
            operation["requestBody"] = {"required": True, "content": {"application/json": {"schema": ref(request)}}}
        document["paths"].setdefault(path, {})[method] = operation
        return operation

    endpoint("get", "/", "API information", "ApiInfo", public=True)
    for path in ("/health", "/live", "/ready"):
        op = endpoint("get", path, "Database readiness" if path == "/ready" else "Process liveness", "HealthResponse", public=True)
        op["responses"] = {"200": op["responses"]["200"]}
        if path == "/ready":
            op["responses"]["503"] = {"description": "Database unavailable", "content": {"application/json": {"schema": ref("HealthResponse")}}}
    endpoint("post", "/auth/register", "Register a CUSTOMER", "AuthResponse", "RegisterRequest", public=True, status=201, rate=True)
    endpoint("post", "/auth/login", "Login with email or phone", "AuthResponse", "LoginRequest", public=True, rate=True)
    endpoint("post", "/auth/refresh", "Rotate a refresh token", "AuthResponse", "RefreshTokenRequest", public=True, rate=True,
             description="Consume A atomically and return B. Replay of A returns 401 and does not revoke B. Serialize refresh requests on the client.")
    endpoint("post", "/auth/logout", "Revoke a refresh token", request="RefreshTokenRequest", public=True, status=204, rate=True,
             description="Idempotent. Revokes the submitted refresh token. An existing access JWT remains valid until expiration.")
    endpoint("post", "/auth/logout-all", "Revoke all own refresh tokens", status=204)
    for path in ("/auth/me", "/users/me"):
        endpoint("get", path, "Current user profile", "MeResponse")
    endpoint("get", "/addresses", "List own addresses", "[]AddressResponse", paged=True)
    endpoint("post", "/addresses", "Create own address", "AddressResponse", "AddressRequest", status=201)
    endpoint("get", "/addresses/{id}", "Read own address", "AddressResponse")
    endpoint("put", "/addresses/{id}", "Replace own address", "AddressResponse", "AddressRequest", description="Addresses used by orders or restaurants are immutable; create a new address instead (409).")
    endpoint("delete", "/addresses/{id}", "Delete own unused address", status=204)
    endpoint("post", "/addresses/{id}/default", "Set own default address", "AddressResponse")
    endpoint("get", "/restaurants", "List active restaurants", "[]RestaurantResponse", public=True, paged=True)
    endpoint("get", "/restaurants/{id}", "Active restaurant detail", "RestaurantResponse", public=True)
    endpoint("get", "/restaurants/mine", "List owned restaurants", "[]RestaurantResponse", paged=True)
    endpoint("post", "/restaurants", "Create an INACTIVE restaurant", "RestaurantResponse", "RestaurantRequest", status=201, description="RESTAURANT or ADMIN; address must belong to the actor.")
    endpoint("get", "/restaurants/{id}/manage", "Owner or admin restaurant detail", "RestaurantResponse")
    endpoint("put", "/restaurants/{id}", "Update owned restaurant", "RestaurantResponse", "RestaurantRequest")
    endpoint("patch", "/restaurants/{id}/status", "Set restaurant status", "RestaurantResponse", "StatusRequest", description="Owner: ACTIVE or INACTIVE, except suspended restaurants. ADMIN: ACTIVE, INACTIVE or SUSPENDED.")
    endpoint("get", "/restaurants/{id}/categories", "List active categories", "[]CategoryResponse", public=True, paged=True)
    endpoint("get", "/restaurants/{id}/management/categories", "List all owned categories", "[]CategoryResponse", paged=True)
    endpoint("post", "/restaurants/{id}/categories", "Create owned category", "CategoryResponse", "CategoryRequest", status=201)
    endpoint("put", "/restaurants/{id}/categories/{categoryId}", "Update owned category", "CategoryResponse", "CategoryRequest")
    endpoint("patch", "/restaurants/{id}/categories/{categoryId}/active", "Activate or deactivate category", "CategoryResponse", "ActiveRequest")
    op = endpoint("get", "/restaurants/{id}/products", "Search restaurant products", "[]ProductResponse", public=True, paged=True)
    op["parameters"] += [{"name": "categoryId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
                         {"name": "available", "in": "query", "schema": {"type": "boolean"}},
                         {"name": "search", "in": "query", "schema": {"type": "string", "maxLength": 200}, "description": "Case-sensitive name substring; SQL LIKE wildcards are supported."}]
    endpoint("post", "/restaurants/{id}/products", "Create owned product", "ProductResponse", "ProductRequest", status=201)
    endpoint("put", "/restaurants/{id}/products/{productId}", "Update owned product", "ProductResponse", "ProductRequest")
    endpoint("patch", "/restaurants/{id}/products/{productId}/availability", "Set product availability", "ProductResponse", "AvailabilityRequest")
    endpoint("get", "/restaurants/{id}/orders", "List owned restaurant orders", "[]OrderSummary", paged=True)
    endpoint("get", "/orders", "List own customer orders", "[]OrderSummary", paged=True)
    op = endpoint("post", "/orders", "Create a cash order", "OrderResponse", "OrderRequest", status=201,
                  description="CUSTOMER permission. Server calculates all prices and configured delivery fee. Reusing the same key and request returns the same order with its current state; a different request with that key returns 409.")
    op["parameters"] = [{"name": "Idempotency-Key", "in": "header", "required": False, "schema": {"type": "string", "pattern": "^[A-Za-z0-9_-]{1,100}$"}, "description": "Recommended for safe retries; scoped to the authenticated user and retained with the order."}]
    endpoint("get", "/orders/{id}", "Read an authorized order and history", "OrderResponse", description="Customer owner, restaurant owner, assigned driver or ADMIN. Includes delivery/pickup addresses and customer phone only after authorization.")
    endpoint("post", "/orders/{id}/status", "Advance or cancel an order", "OrderResponse", "StatusRequest", description="Restaurant: CONFIRMED, PREPARING, READY. Assigned driver: DELIVERED. Customer owner, restaurant owner or ADMIN: CANCELLED only from RECEIVED/CONFIRMED/PREPARING. Each change is transactional and recorded in history. Use driver acceptance for READY → IN_DELIVERY; invalid transitions return 409.")
    endpoint("get", "/driver/me", "Current driver profile", "DriverResponse")
    endpoint("patch", "/driver/status", "Set own driver availability", "DriverResponse", "StatusRequest", description="AVAILABLE or OFFLINE; BUSY and SUSPENDED drivers cannot change their own status.")
    endpoint("get", "/driver/orders", "List assigned orders", "[]OrderSummary", paged=True)
    endpoint("get", "/driver/orders/available", "List ready unassigned orders", "[]OrderSummary", paged=True, description="Requires an AVAILABLE driver.")
    endpoint("post", "/driver/orders/{id}/accept", "Accept an order atomically", "OrderResponse", description="Only one driver can win. Sets IN_DELIVERY and driver BUSY atomically. Retrying the winning assignment while IN_DELIVERY is safe.")
    endpoint("get", "/admin/users", "List users without password hashes", "[]MeResponse", paged=True)
    endpoint("put", "/admin/users/{id}", "Set user roles and active state", "MeResponse", "AdminUserRequest", description="ADMIN only. Revokes refresh sessions. Adding DRIVER provisions an OFFLINE profile. Cannot remove own administrative access or disable a busy driver.")
    endpoint("patch", "/admin/drivers/{id}/status", "Administratively set driver status", "DriverResponse", "StatusRequest", description="id is the driver's USER ID. ADMIN only; OFFLINE, AVAILABLE or SUSPENDED. BUSY is managed by the order workflow.")
    endpoint("patch", "/admin/restaurants/{id}/status", "Administratively set restaurant status", "RestaurantResponse", "StatusRequest", description="ADMIN only; ACTIVE, INACTIVE or SUSPENDED.")
    return document


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    document = build()
    output = json.dumps(document, indent=2, ensure_ascii=False) + "\n"
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != output:
            raise SystemExit("OpenAPI is stale: run python scripts/generate_openapi.py")
    else:
        OUTPUT.write_text(output, encoding="utf-8")
    print(f"OpenAPI: {sum(len(methods) for methods in document['paths'].values())} operations, {len(document['components']['schemas'])} schemas")
