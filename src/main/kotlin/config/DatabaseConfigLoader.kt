package com.blackneko.config

import com.blackneko.infrastructure.database.DatabaseConfig
import io.ktor.server.config.ApplicationConfig

fun ApplicationConfig.databaseConfig(): DatabaseConfig =
    DatabaseConfig(
        host =
            property("database.host")
                .getString(),

        port =
            property("database.port")
                .getString()
                .toInt(),

        database =
            property("database.name")
                .getString(),

        user =
            property("database.user")
                .getString(),

        password =
            property("database.password")
                .getString(),

        poolSize =
            property("database.poolSize")
                .getString()
                .toInt()
    )