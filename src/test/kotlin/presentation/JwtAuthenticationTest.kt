package com.blackneko.presentation

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.blackneko.config.configureSerialization
import com.blackneko.config.configureStatusPages
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

class JwtAuthenticationTest {
    private val config = JwtConfig("delivery-test", "delivery-client", "Test", "test-secret-with-at-least-32-characters", 15, 30)

    @Test
    fun `production verifier rejects missing forged expired and incorrect claims`() = testApplication {
        environment { config = MapApplicationConfig() }
        application {
            configureSerialization()
            configureStatusPages()
            configureAuthentication(this@JwtAuthenticationTest.config)
            routing { authenticate(JWT_AUTH) { get("/protected") { call.respondText("ok") } } }
        }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/protected").status)
        assertEquals(HttpStatusCode.OK, client.get("/protected") { bearerAuth(token()) }.status)
        val valid = token()
        val segments = valid.split('.').toMutableList()
        segments[2] = (if (segments[2].first() == 'a') "b" else "a") + segments[2].drop(1)
        val rejected = listOf(
            segments.joinToString("."),
            token(secret = "wrong-secret"),
            token(issuer = "wrong-issuer"),
            token(audience = "wrong-audience"),
            token(expiry = Instant.now().minusSeconds(60)),
            token(expiry = null),
            token(subject = "invalid-uuid"),
            token(roles = emptyList()),
            token(roles = listOf("SUPERADMIN"))
        )
        rejected.forEach { assertEquals(HttpStatusCode.Unauthorized, client.get("/protected") { bearerAuth(it) }.status) }
    }

    private fun token(secret: String = config.secret, issuer: String = config.issuer, audience: String = config.audience,
                      expiry: Instant? = Instant.now().plusSeconds(300), subject: String = UUID.randomUUID().toString(),
                      roles: List<String> = listOf("CUSTOMER")): String {
        val builder = JWT.create().withIssuer(issuer).withAudience(audience).withSubject(subject).withClaim("roles", roles)
        expiry?.let { builder.withExpiresAt(it) }
        return builder.sign(Algorithm.HMAC256(secret))
    }
}
