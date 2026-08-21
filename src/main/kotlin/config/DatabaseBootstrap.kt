package com.blackneko.config

import com.blackneko.infrastructure.database.DatabaseFactory
import com.blackneko.infrastructure.database.FlywayFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import org.jetbrains.exposed.v1.jdbc.Database

data class DatabaseResources(
    val dataSource: HikariDataSource,
    val database: Database
)

fun Application.bootstrapDatabase():
        DatabaseResources {

    val databaseConfig =
        environment.config
            .databaseConfig()

    log.info(
        "Initializing database at {}:{} / {}",
        databaseConfig.host,
        databaseConfig.port,
        databaseConfig.database
    )

    val dataSource =
        DatabaseFactory.createDataSource(
            databaseConfig
        )

    try {

        FlywayFactory.migrate(
            dataSource
        )

        val database =
            DatabaseFactory.connect(
                dataSource
            )

        monitor.subscribe(
            ApplicationStopping
        ) {

            log.info(
                "Closing database connection pool"
            )

            dataSource.close()
        }

        return DatabaseResources(
            dataSource = dataSource,
            database = database
        )

    } catch (exception: Throwable) {

        dataSource.close()

        throw exception
    }
}