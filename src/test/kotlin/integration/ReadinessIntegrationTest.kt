package com.blackneko.integration

import com.blackneko.configureApplication
import com.blackneko.application.ReadinessProbe
import com.blackneko.config.securityModule
import com.blackneko.infrastructure.database.*
import com.blackneko.infrastructure.security.JwtConfig
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.*

class ReadinessIntegrationTest {
    @Test
    fun `readiness detects real PostgreSQL outage while liveness stays up`() {
        PostgreSQLContainer("postgres:17-alpine").apply {
            withDatabaseName("readiness_test"); withUsername("readiness"); withPassword("isolated-test-password")
        }.use { postgres ->
            postgres.start()
            val settings = DatabaseConfig(postgres.host, postgres.firstMappedPort, postgres.databaseName, postgres.username, postgres.password, 2)
            DatabaseFactory.createDataSource(settings).use { pool ->
                testApplication {
                    environment { config = MapApplicationConfig() }
                    application {
                        configureApplication(listOf(securityModule(JwtConfig("test", "client", "Test", "readiness-test-secret-at-least-32-characters", 15, 30)),
                            module { single<ReadinessProbe> { DatabaseReadinessProbe(pool) } }))
                    }
                    assertEquals(HttpStatusCode.OK, client.get("/ready").status)
                    assertEquals(HttpStatusCode.OK, client.get("/live").status)
                    postgres.stop()
                    val unavailable = client.get("/ready")
                    assertEquals(HttpStatusCode.ServiceUnavailable, unavailable.status)
                    val body = unavailable.bodyAsText()
                    assertTrue(body.contains("DOWN"))
                    assertFalse(body.contains("postgres", ignoreCase = true))
                    assertFalse(body.contains("password", ignoreCase = true))
                    assertEquals(HttpStatusCode.OK, client.get("/live").status)
                }
            }
        }
    }
}
