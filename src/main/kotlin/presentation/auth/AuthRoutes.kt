
package com.blackneko.presentation.auth

import com.blackneko.application.auth.*
import com.blackneko.presentation.auth.dto.*
import com.blackneko.presentation.security.JWT_AUTH
import com.blackneko.presentation.security.authenticatedUser
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {

    val registerUserUseCase
            by inject<RegisterUserUseCase>()

    val loginUseCase
            by inject<LoginUseCase>()

    val getCurrentUserUseCase
            by inject<GetCurrentUserUseCase>()

    route("/auth") {

        post("/register") {

            val request =
                call.receive<RegisterRequest>()

            val result =
                registerUserUseCase(
                    RegisterUserCommand(
                        email =
                            request.email,

                        phone =
                            request.phone,

                        password =
                            request.password,

                        firstName =
                            request.firstName,

                        lastName =
                            request.lastName
                    )
                )

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    userId =
                        result.userId
                            .toString(),

                    roles =
                        listOf(
                            "CUSTOMER"
                        ),

                    accessToken =
                        result.accessToken
                )
            )
        }

        post("/login") {

            val request =
                call.receive<LoginRequest>()

            val result =
                loginUseCase(
                    LoginCommand(
                        identifier =
                            request.identifier,

                        password =
                            request.password
                    )
                )

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    userId =
                        result.userId
                            .toString(),

                    roles =
                        result.roles.map {
                            it.name
                        },

                    accessToken =
                        result.accessToken
                )
            )
        }

        authenticate(
            JWT_AUTH
        ) {

            get("/me") {

                val authenticated =
                    call.authenticatedUser()

                val user =

                    getCurrentUserUseCase(
                        authenticated.userId
                    )

                call.respond(
                    MeResponse(
                        id =
                            user.id.toString(),

                        email =
                            user.email,

                        phone =
                            user.phone,

                        firstName =
                            user.firstName,

                        lastName =
                            user.lastName,

                        roles =
                            user.roles
                                .map {
                                    it.name
                                },

                        isActive =
                            user.isActive
                    )
                )
            }
        }
    }
}
