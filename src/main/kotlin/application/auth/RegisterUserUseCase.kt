package com.blackneko.application.auth

import com.blackneko.application.TransactionRunner
import com.blackneko.application.exception.ConflictException
import com.blackneko.application.security.PasswordHasher
import com.blackneko.application.security.TokenService
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.domain.user.UserRepository
import java.time.Clock
import java.util.UUID

class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val transactionRunner: TransactionRunner,
    private val createAuthSession: CreateAuthSessionUseCase,
    private val clock: Clock
) {

    suspend operator fun invoke(
        command: RegisterUserCommand
    ): RegisterUserResult {

        validate(
            command
        )

        return transactionRunner.transaction {

            if (
                userRepository.existsByPhone(
                    command.phone
                )
            ) {
                throw ConflictException(
                    "Phone is already registered"
                )
            }

            command.email?.let { email ->

                if (
                    userRepository.existsByEmail(
                        email
                    )
                ) {
                    throw ConflictException(
                        "Email is already registered"
                    )
                }
            }

            val now =
                clock.instant()

            val user =
                User(
                    id =
                        UUID.randomUUID(),

                    email =
                        command.email
                            ?.trim()
                            ?.lowercase(),

                    phone =
                        command.phone.trim(),

                    passwordHash =
                        passwordHasher.hash(
                            command.password
                        ),

                    firstName =
                        command.firstName.trim(),

                    lastName =
                        command.lastName.trim(),

                    roles =
                        setOf(
                            Role.CUSTOMER
                        ),

                    isActive =
                        true,

                    createdAt =
                        now,

                    updatedAt =
                        now
                )

            userRepository.save(
                user
            )
            val session =
                createAuthSession(user)

            RegisterUserResult(
                userId =
                    user.id,

                accessToken =
                    tokenService
                        .generateAccessToken(
                            user
                        ),
                refreshToken =
                    session.refreshToken
            )
        }
    }

    private fun validate(
        command: RegisterUserCommand
    ) {

        require(
            command.phone.isNotBlank()
        ) {
            "Phone is required"
        }

        require(
            command.firstName.isNotBlank()
        ) {
            "First name is required"
        }

        require(
            command.lastName.isNotBlank()
        ) {
            "Last name is required"
        }

        require(
            command.password.length >= 8
        ) {
            "Password must contain at least 8 characters"
        }
    }
}