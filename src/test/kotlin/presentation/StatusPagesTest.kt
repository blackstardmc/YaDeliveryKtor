package com.blackneko.presentation

import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.exception.AuthorizationException
import com.blackneko.application.exception.ConflictException
import com.blackneko.application.exception.NotFoundException
import com.blackneko.application.exception.ValidationException
import com.blackneko.config.configureStatusPages
import com.blackneko.config.configureSerialization
import io.ktor.client.statement.bodyAsText
import kotlin.test.assertFalse
import com.blackneko.presentation.common.ErrorResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class StatusPagesTest {

    @Test
    fun `validation exception should return 400`() =

        testApplication {

            configureApplication()

            val testClient =
                jsonClient()

            val response =
                testClient.get(
                    "/validation"
                )

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            val body =
                response.body<ErrorResponse>()

            assertEquals(
                "VALIDATION_ERROR",
                body.error
            )

            assertEquals(
                "Invalid email",
                body.message
            )

            assertEquals(
                "email",
                body.field
            )
        }
    private fun ApplicationTestBuilder.jsonClient() =
        createClient {

            install(
                io.ktor.client.plugins.contentnegotiation.ContentNegotiation
            ) {
                json(
                    Json {
                        ignoreUnknownKeys = false
                    }
                )
            }
        }
    @Test
    fun `authentication exception should return 401`() =
        testApplication {

            configureApplication()
            val testClient =
                jsonClient()

            val response =
                testClient.get("/authentication")


            assertEquals(
                HttpStatusCode.Unauthorized,
                response.status
            )

            val body =
                response.body<ErrorResponse>()

            assertEquals(
                "UNAUTHORIZED",
                body.error
            )

            assertEquals(
                "Invalid credentials",
                body.message
            )

            assertNull(
                body.field
            )
        }

    @Test
    fun `authorization exception should return 403`() =
        testApplication {

            configureApplication()

            val testClient =
                jsonClient()

            val response =
                testClient.get("/authorization")

            assertEquals(
                HttpStatusCode.Forbidden,
                response.status
            )

            val body =
                response.body<ErrorResponse>()

            assertEquals(
                "FORBIDDEN",
                body.error
            )
        }

    @Test
    fun `not found exception should return 404`() =
        testApplication {

            configureApplication()

            val testClient =
                jsonClient()

            val response =
                testClient.get("/not-found")

            assertEquals(
                HttpStatusCode.NotFound,
                response.status
            )

            val body =
                response.body<ErrorResponse>()

            assertEquals(
                "NOT_FOUND",
                body.error
            )
        }

    @Test
    fun `conflict exception should return 409`() =
        testApplication {

            configureApplication()

            val testClient =
                jsonClient()

            val response =
                testClient.get("/conflict")

            assertEquals(
                HttpStatusCode.Conflict,
                response.status
            )

            val body =
                response.body<ErrorResponse>()

            assertEquals(
                "CONFLICT",
                body.error
            )
        }

    @Test
    fun `unexpected exception should return safe 500`() =
        testApplication {

            configureApplication()

            val testClient =
                jsonClient()

            val response =
                testClient.get("/unexpected")

            assertEquals(
                HttpStatusCode.InternalServerError,
                response.status
            )

            val body =
                response.body<ErrorResponse>()

            assertEquals(
                "INTERNAL_ERROR",
                body.error
            )

            assertEquals(
                "An unexpected error occurred",
                body.message
            )

            val text = response.bodyAsText()
            listOf("SECRET", "DATABASE", "SELECT", "password", "RuntimeException", "at com.").forEach {
                assertFalse(text.contains(it), "Internal details leaked: $it")
            }

            /*
             * Lo importante:
             *
             * El cliente NO debe recibir:
             *
             * PostgreSQL
             * passwords
             * SQL
             * stack traces
             * nombres internos
             */
        }

    private fun ApplicationTestBuilder.configureApplication() {

        application {




            configureSerialization()
            configureStatusPages()

            routing {

                get("/validation") {

                    throw ValidationException(
                        message = "Invalid email",
                        field = "email",
                    )
                }

                get("/authentication") {

                    throw AuthenticationException()
                }

                get("/authorization") {

                    throw AuthorizationException()
                }

                get("/not-found") {

                    throw NotFoundException(
                        "User not found"
                    )
                }

                get("/conflict") {

                    throw ConflictException(
                        "Email already exists"
                    )
                }

                get("/unexpected") {

                    throw RuntimeException(
                        "SECRET DATABASE INFORMATION"
                    )
                }
            }
        }



    }
}
