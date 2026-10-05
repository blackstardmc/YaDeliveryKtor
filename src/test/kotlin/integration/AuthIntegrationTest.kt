package com.blackneko.integration

import com.blackneko.configureApplication
import com.blackneko.config.infrastructureModule
import com.blackneko.config.securityModule
import com.blackneko.config.marketplaceModule
import com.blackneko.infrastructure.database.DatabaseConfig
import com.blackneko.infrastructure.database.repository.*
import com.blackneko.infrastructure.security.JwtConfig
import com.blackneko.infrastructure.security.SecureRefreshTokenGenerator
import com.blackneko.presentation.auth.dto.*
import com.blackneko.domain.user.Role
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class AuthIntegrationTest : DatabaseIntegrationTest() {
    private val jwt = JwtConfig("auth-e2e", "auth-client", "Test", "isolated-test-secret-at-least-32-characters", 15, 30)
    private val generator = SecureRefreshTokenGenerator()
    private val users by lazy { UserRepositoryImpl(transactionRunner) }
    private val tokens by lazy { RefreshTokenRepositoryImpl(transactionRunner) }

    private fun ApplicationTestBuilder.setup(limit: Int = 100) {
        environment { config = MapApplicationConfig() }
        application {
            configureApplication(listOf(
                infrastructureModule(DatabaseConfig(postgres.host, postgres.firstMappedPort, postgres.databaseName,
                    postgres.username, postgres.password, 5), dataSource, database),
                securityModule(jwt), marketplaceModule()
            ), authLimit = limit)
        }
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    private fun registration(email: String = "ana@test.com", phone: String = "+5355551234") =
        RegisterRequest(email, phone, "Password123!", "Ana", "Perez")

    @Test
    fun `register login me rotation replay and logout end to end`() = testApplication {
        setup()
        val http = jsonClient()
        val registered = http.post("/auth/register") { contentType(ContentType.Application.Json); setBody(registration()) }
        assertEquals(HttpStatusCode.Created, registered.status)
        val initial = registered.body<AuthResponse>()
        val persisted = assertNotNull(users.findByEmail("ana@test.com"))
        assertEquals(setOf(Role.CUSTOMER), persisted.roles)
        assertNotEquals("Password123!", persisted.passwordHash)
        assertFalse(registered.bodyAsText().contains("password"))
        assertNotNull(tokens.findByHash(generator.hash(initial.refreshToken)))
        assertNull(tokens.findByHash(initial.refreshToken))

        val login = http.post("/auth/login") {
            contentType(ContentType.Application.Json); setBody(LoginRequest(" ANA@TEST.COM ", "Password123!"))
        }
        assertEquals(HttpStatusCode.OK, login.status)
        val a = login.body<AuthResponse>()
        assertEquals(initial.userId, a.userId)
        assertEquals(HttpStatusCode.Unauthorized, http.get("/auth/me").status)
        val me = http.get("/auth/me") { bearerAuth(a.accessToken) }
        assertEquals(HttpStatusCode.OK, me.status)
        assertEquals(a.userId, me.body<MeResponse>().id)
        assertFalse(me.bodyAsText().contains("password", ignoreCase = true))

        suspend fun refresh(token: String) = http.post("/auth/refresh") {
            contentType(ContentType.Application.Json); setBody(RefreshTokenRequest(token))
        }
        val rotated = refresh(a.refreshToken)
        assertEquals(HttpStatusCode.OK, rotated.status)
        val b = rotated.body<AuthResponse>()
        assertNotEquals(a.refreshToken, b.refreshToken)
        assertNotNull(tokens.findByHash(generator.hash(a.refreshToken))?.revokedAt)
        assertEquals(HttpStatusCode.Unauthorized, refresh(a.refreshToken).status)
        val next = refresh(b.refreshToken)
        assertEquals(HttpStatusCode.OK, next.status)
        val c = next.body<AuthResponse>()
        assertEquals(HttpStatusCode.NoContent, http.post("/auth/logout") {
            contentType(ContentType.Application.Json); setBody(RefreshTokenRequest(c.refreshToken))
        }.status)
        assertEquals(HttpStatusCode.Unauthorized, refresh(c.refreshToken).status)
    }

    @Test
    fun `duplicates normalized identifiers invalid bodies and privilege injection`() = testApplication {
        setup()
        val http = jsonClient()
        suspend fun register(request: RegisterRequest) = http.post("/auth/register") {
            contentType(ContentType.Application.Json); setBody(request)
        }
        assertEquals(HttpStatusCode.Created, register(registration()).status)
        assertEquals(HttpStatusCode.Conflict, register(registration(phone = "+5355551235")).status)
        assertEquals(HttpStatusCode.Conflict, register(registration(email = " ANA@TEST.COM ", phone = "+5355551235")).status)
        assertEquals(HttpStatusCode.Conflict, register(registration(email = "other@test.com")).status)
        assertEquals(HttpStatusCode.BadRequest, register(registration().copy(password = "short")).status)
        assertEquals(HttpStatusCode.BadRequest, register(registration().copy(email = "bad")).status)
        val injected = http.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"evil@test.com","phone":"+5355559999","password":"Password123!","firstName":"Evil","lastName":"User","roles":["ADMIN"]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, injected.status)
        assertNull(users.findByEmail("evil@test.com"))
        assertEquals(HttpStatusCode.BadRequest, http.post("/auth/login") {
            contentType(ContentType.Application.Json); setBody("{invalid-json")
        }.status)
    }

    @Test
    fun `login failures do not enumerate users and inactive user rejected`() = testApplication {
        setup()
        val http = jsonClient()
        http.post("/auth/register") { contentType(ContentType.Application.Json); setBody(registration()) }
        suspend fun login(identifier: String, password: String) = http.post("/auth/login") {
            contentType(ContentType.Application.Json); setBody(LoginRequest(identifier, password))
        }
        val wrong = login("ana@test.com", "wrong")
        val absent = login("absent@test.com", "wrong")
        assertEquals(HttpStatusCode.Unauthorized, wrong.status)
        assertEquals(wrong.status, absent.status)
        assertEquals(wrong.bodyAsText(), absent.bodyAsText())
        val user = assertNotNull(users.findByEmail("ana@test.com"))
        users.update(user.copy(isActive = false))
        assertEquals(HttpStatusCode.Unauthorized, login("ana@test.com", "Password123!").status)
    }

    @Test
    fun `fourth login attempt is rate limited`() = testApplication {
        setup(limit = 3)
        val http = jsonClient()
        repeat(3) {
            assertEquals(HttpStatusCode.Unauthorized, http.post("/auth/login") {
                contentType(ContentType.Application.Json); setBody(LoginRequest("absent@test.com", "wrong"))
            }.status)
        }
        val limited = http.post("/auth/login") {
            contentType(ContentType.Application.Json); setBody(LoginRequest("absent@test.com", "wrong"))
            header("X-Forwarded-For", "spoofed-address")
        }
        assertEquals(HttpStatusCode.TooManyRequests, limited.status)
        assertNotNull(limited.headers["Retry-After"])
        assertTrue(limited.bodyAsText().contains("RATE_LIMITED"))
    }
}
