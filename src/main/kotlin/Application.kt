package com.blackneko

import com.blackneko.config.bootstrapDatabase
import com.blackneko.config.configureDatabase
import com.blackneko.config.configureKoin
import com.blackneko.config.configureSerialization
import com.blackneko.config.configureStatusPages
import com.blackneko.config.configureHttpSecurity
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import com.blackneko.infrastructure.database.DatabaseConfig
import com.blackneko.infrastructure.database.DatabaseFactory
import com.blackneko.infrastructure.database.FlywayFactory
import com.blackneko.presentation.configureRouting
import com.blackneko.presentation.security.configureAuthentication
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

    configureApplication()
}

/** Tests supply isolated repositories; production bootstraps the database in module(). */
fun Application.configureApplication(dependencies: List<Module>, authLimit: Int = 20) {
    install(Koin) { modules(dependencies) }
    configureApplication(authLimit)
}

fun Application.configureApplication(authLimit: Int = 20) {
    configureSerialization()
    configureAuthentication()
    configureStatusPages()
    configureHttpSecurity(authLimit)
    configureRouting()
}
