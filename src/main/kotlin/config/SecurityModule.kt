package com.blackneko.config

import com.blackneko.application.auth.GetCurrentUserUseCase
import com.blackneko.application.auth.LoginUseCase
import com.blackneko.application.auth.RefreshSessionUseCase
import com.blackneko.application.auth.RegisterUserUseCase
import com.blackneko.application.security.BCryptPasswordHasher
import com.blackneko.infrastructure.security.JwtConfig
import com.blackneko.application.security.JwtTokenService
import com.blackneko.application.security.PasswordHasher
import com.blackneko.application.security.RefreshTokenGenerator
import com.blackneko.application.security.TokenService
import com.blackneko.application.security.TokenSettings
import com.blackneko.domain.auth.RefreshTokenRepository
import com.blackneko.infrastructure.database.repository.RefreshTokenRepositoryImpl
import com.blackneko.infrastructure.security.SecureRefreshTokenGenerator

import org.koin.dsl.module
import java.time.Clock
import kotlin.math.sin

fun securityModule(
    jwtConfig: JwtConfig
) = module {

    single<JwtConfig> {
        jwtConfig
    }

    single {
        TokenSettings(
            refreshTokenExpirationDays =
                get<JwtConfig>()
                    .refreshTokenExpirationDays
        )
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
    single<RefreshSessionUseCase> {
        RefreshSessionUseCase(
            refreshTokenRepository = get(),
            userRepository = get(),
            clock = get(),
            refreshTokenGenerator = get(),
            tokenService = get(),
            transactionRunner = get(),
            settings = get()

        )
    }

    single<RefreshTokenGenerator> {
        SecureRefreshTokenGenerator()
    }

    single {
        RegisterUserUseCase(
            userRepository = get(),
            passwordHasher = get(),
            tokenService = get(),
            transactionRunner = get(),
            clock = get(),
            createAuthSession = get()
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
            tokenService = get(),
            createAuthSession = get(),
        )
    }
}