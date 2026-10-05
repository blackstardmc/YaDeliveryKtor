package com.blackneko.application.auth

import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.security.PasswordHasher
import com.blackneko.application.security.TokenService
import com.blackneko.domain.user.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val createAuthSession: CreateAuthSessionUseCase
) {

    suspend operator fun invoke(
        command: LoginCommand
    ): LoginResult {

        LoginValidator.validate(command)

        val identifier =
            command.identifier.trim()

        val user =
            if ("@" in identifier) {

                userRepository.findByEmail(
                    identifier.lowercase()
                )

            } else {

                userRepository.findByPhone(
                    identifier
                )
            }

        val validPassword =
            passwordHasher.verify(
                password =
                    command.password,

                hash =
                    user?.passwordHash ?: dummyPasswordHash
            )

        if (user == null || !user.isActive || !validPassword) {
            throw AuthenticationException()
        }
        val session =
            createAuthSession(user)

        return LoginResult(
            userId =
                user.id,

            roles =
                user.roles,

            accessToken =
                    session.accessToken,
            refreshToken = session.refreshToken
        )
    }

    // Perform BCrypt verification for missing users too, to reduce timing-based enumeration.
    private val dummyPasswordHash by lazy { passwordHasher.hash(java.util.UUID.randomUUID().toString()) }
}
