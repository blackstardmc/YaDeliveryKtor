package com.blackneko

import com.blackneko.config.configureDatabase
import com.blackneko.config.configureKoin
import com.blackneko.config.configureSerialization
import com.blackneko.config.configureStatusPages
import com.blackneko.infrastructure.database.DatabaseConfig
import com.blackneko.infrastructure.database.DatabaseFactory
import com.blackneko.infrastructure.database.FlywayFactory
import com.blackneko.presentation.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config =
        DatabaseConfig(

            host = environment.config
                .property("database.host")
                .getString(),

            port = environment.config
                .property("database.port")
                .getString()
                .toInt(),

            database = environment.config
                .property("database.name")
                .getString(),

            user = environment.config
                .property("database.user")
                .getString(),

            password = environment.config
                .property("database.password")
                .getString()
        )

    FlywayFactory.migrate(config)

    DatabaseFactory.connect(config)

    configureKoin()

    configureDatabase()

    configureSerialization()

    configureStatusPages()

    configureRouting()
}