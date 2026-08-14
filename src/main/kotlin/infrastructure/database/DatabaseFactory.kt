package com.blackneko.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {

    fun connect(
        config: DatabaseConfig
    ) {

        val hikari =
            HikariConfig().apply {

                jdbcUrl = config.jdbcUrl

                driverClassName =
                    "org.postgresql.Driver"

                username = config.user

                password = config.password

                maximumPoolSize = 10

                minimumIdle = 2

                isAutoCommit = false

                transactionIsolation =
                    "TRANSACTION_REPEATABLE_READ"

                validate()
            }

        Database.connect(
            HikariDataSource(hikari)
        )
    }
}