package com.blackneko

import com.blackneko.config.bootstrapDatabase
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

fun main(
    args: Array<String>
) {

    EngineMain.main(args)
}

fun Application.module() {

    val databaseResources =
        bootstrapDatabase()

    configureKoin(
        dataSource =
            databaseResources.dataSource,

        database =
            databaseResources.database
    )

    configureSerialization()

    configureStatusPages()

   configureRouting()
}