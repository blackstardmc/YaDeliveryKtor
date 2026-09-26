package com.blackneko.config

import com.blackneko.application.auth.GetCurrentUserUseCase
import com.blackneko.application.auth.LoginUseCase
import com.blackneko.application.auth.RegisterUserUseCase
import com.blackneko.application.security.BCryptPasswordHasher
import com.blackneko.application.security.JwtConfig
import com.blackneko.application.security.JwtTokenService
import com.blackneko.application.security.PasswordHasher
import com.blackneko.application.security.TokenService

import org.koin.dsl.module
import java.time.Clock

fun securityModule(
    jwtConfig: JwtConfig
) = module {

    single<JwtConfig> {
        jwtConfig
    }

    single<PasswordHasher> {
        BCryptPasswordHasher()
    }

    single<TokenService> {
        JwtTokenService(
            config = get()
        )
    }

    single<Clock> {
        Clock.systemUTC()
    }

    single {
        RegisterUserUseCase(
            userRepository = get(),
            passwordHasher = get(),
            tokenService = get(),
            transactionRunner = get(),
            clock = get()
        )
    }

    single {
        GetCurrentUserUseCase(
            userRepository = get()
        )
    }

    single {
        LoginUseCase(
            userRepository = get(),
            passwordHasher = get(),
            tokenService = get()
        )
    }
}