package com.blackneko.application.auth

import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.security.PasswordHasher
import com.blackneko.application.security.TokenService
import com.blackneko.domain.user.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService
) {

    suspend operator fun invoke(
        command: LoginCommand
    ): LoginResult {

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

        if (user == null) {
            throw AuthenticationException()
        }

        if (!user.isActive) {
            throw AuthenticationException()
        }

        val validPassword =
            passwordHasher.verify(
                password =
                    command.password,

                hash =
                    user.passwordHash
            )

        if (!validPassword) {
            throw AuthenticationException()
        }

        return LoginResult(
            userId =
                user.id,

            roles =
                user.roles,

            accessToken =
                tokenService.generateAccessToken(
                    user
                )
        )
    }
}