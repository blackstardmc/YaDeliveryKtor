package com.blackneko.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {

    fun createDataSource(
        config: DatabaseConfig
    ): HikariDataSource {

        val hikariConfig =
            HikariConfig().apply {

                jdbcUrl =
                    config.jdbcUrl

                driverClassName =
                    "org.postgresql.Driver"

                username =
                    config.user

                password =
                    config.password

                maximumPoolSize =
                    config.poolSize

                minimumIdle = 2

                isAutoCommit = false

                transactionIsolation =
                    "TRANSACTION_READ_COMMITTED"

                connectionTimeout =
                    10_000

                validationTimeout =
                    5_000

                initializationFailTimeout =
                    10_000

                poolName =
                    "delivery-hikari"

                addDataSourceProperty(
                    "reWriteBatchedInserts",
                    "true"
                )
            }

        return HikariDataSource(
            hikariConfig
        )
    }

    fun connect(
        dataSource: HikariDataSource
    ): Database {

        return Database.connect(
            datasource = dataSource
        )
    }
}