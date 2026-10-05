package com.blackneko.presentation

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.blackneko.config.configureSerialization
import com.blackneko.config.configureStatusPages
import com.blackneko.domain.security.Permission
import com.blackneko.infrastructure.security.JwtConfig
import com.blackneko.presentation.security.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class AuthorizationHttpTest {
    private val jwt = JwtConfig("test", "test-client", "Test", "authorization-test-secret-123456789", 15, 30)

    @Test
    fun `production permission checks distinguish 401 403 and combined roles`() = testApplication {
        environment { config = MapApplicationConfig() }
        application {
            configureSerialization()
            configureStatusPages()
            configureAuthentication(jwt)
            routing {
                authenticate(JWT_AUTH) {
                    get("/orders-test") { call.requirePermission(Permission.ORDER_CREATE); call.respondText("ok") }
                    get("/admin-test") { call.requirePermission(Permission.ADMIN_USER_MANAGE); call.respondText("ok") }
                }
            }
        }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/orders-test").status)
        listOf(
            listOf("CUSTOMER") to HttpStatusCode.OK,
            listOf("DRIVER") to HttpStatusCode.Forbidden,
            listOf("ADMIN") to HttpStatusCode.OK,
            listOf("DRIVER", "CUSTOMER") to HttpStatusCode.OK
        ).forEach { (roles, status) ->
            assertEquals(status, client.get("/orders-test") { bearerAuth(token(roles)) }.status)
        }
        assertEquals(HttpStatusCode.Forbidden, client.get("/admin-test") { bearerAuth(token(listOf("CUSTOMER"))) }.status)
        assertEquals(HttpStatusCode.OK, client.get("/admin-test") { bearerAuth(token(listOf("ADMIN"))) }.status)
    }

    private fun token(roles: List<String>) = JWT.create().withIssuer(jwt.issuer).withAudience(jwt.audience)
        .withSubject(UUID.randomUUID().toString()).withClaim("roles", roles)
        .withExpiresAt(Instant.now().plusSeconds(300)).sign(Algorithm.HMAC256(jwt.secret))
}
