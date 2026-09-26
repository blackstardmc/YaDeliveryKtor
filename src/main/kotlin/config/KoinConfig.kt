package com.blackneko.config

import com.blackneko.application.TransactionRunner
import com.blackneko.application.security.jwtConfig
import com.blackneko.infrastructure.database.TransactionRunnerImpl
import com.blackneko.infrastructure.database.providers.ClockProvider
import com.blackneko.infrastructure.database.providers.IdGenerator
import com.blackneko.infrastructure.database.providers.SystemClockProvider
import com.blackneko.infrastructure.database.providers.UUIDGenerator
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin(
    dataSource: HikariDataSource,
    database: Database
) {

    val jwtConfig =
        environment.config
            .jwtConfig()

    val config =
        environment.config
            .databaseConfig()

    install(Koin) {

        slf4jLogger()

        modules(
            infrastructureModule(
                databaseConfig = config,
                dataSource = dataSource,
                database = database
            ),
            securityModule(jwtConfig)
        )
    }
}