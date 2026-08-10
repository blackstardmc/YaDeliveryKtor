package com.blackneko.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {

    val config = environment.config

    val host = config.property("database.host").getString()
    val port = config.property("database.port").getString()
    val database = config.property("database.name").getString()
    val user = config.property("database.user").getString()
    val password = config.property("database.password").getString()

    val poolSize =
        config.property("database.poolSize").getString().toInt()

    val jdbcUrl =
        "jdbc:postgresql://$host:$port/$database"

    val hikariConfig = HikariConfig().apply {

        this.jdbcUrl = jdbcUrl

        username = user
        this.password = password

        maximumPoolSize = poolSize

        minimumIdle = 2

        isAutoCommit = false

        transactionIsolation =
            "TRANSACTION_READ_COMMITTED"

        connectionTimeout = 10_000

        validationTimeout = 5_000

        initializationFailTimeout = 10_000

        poolName = "delivery-hikari"

        addDataSourceProperty(
            "reWriteBatchedInserts",
            "true"
        )
    }

    val dataSource =
        HikariDataSource(hikariConfig)

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    Database.connect(dataSource)

    monitor.subscribe(ApplicationStopping) {

        dataSource.close()
    }

    log.info(
        "Database connected: jdbc:postgresql://$host:$port/$database"
    )
}