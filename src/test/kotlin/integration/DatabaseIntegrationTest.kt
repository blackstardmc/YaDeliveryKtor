package com.blackneko.integration

import com.blackneko.application.TransactionRunner
import com.blackneko.infrastructure.database.DatabaseConfig
import com.blackneko.infrastructure.database.DatabaseFactory
import com.blackneko.infrastructure.database.FlywayFactory
import com.blackneko.infrastructure.database.TransactionRunnerImpl
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class DatabaseIntegrationTest {

    companion object {

        @Container
        @JvmField
        val postgres =
            PostgreSQLContainer("postgres:17-alpine")
                .apply {
                    withDatabaseName("delivery_test")
                    withUsername("delivery")
                    withPassword("delivery_test_password")
                }

        lateinit var dataSource: HikariDataSource

        lateinit var database: Database

        lateinit var transactionRunner: TransactionRunner

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {

            val config =
                DatabaseConfig(
                    host = postgres.host,
                    port = postgres.firstMappedPort,
                    database = postgres.databaseName,
                    user = postgres.username,
                    password = postgres.password,
                    poolSize = 5
                )

            dataSource =
                DatabaseFactory.createDataSource(
                    config
                )

            FlywayFactory.migrate(
                dataSource
            )

            database =
                DatabaseFactory.connect(
                    dataSource
                )

            transactionRunner =
                TransactionRunnerImpl(
                    database
                )
        }

        @JvmStatic
        @AfterAll
        fun shutdownDatabase() {

            if (::dataSource.isInitialized) {
                dataSource.close()
            }
        }
    }

    @BeforeEach
    fun cleanDatabase() {

        DatabaseCleaner.clean(
            database
        )
    }
}