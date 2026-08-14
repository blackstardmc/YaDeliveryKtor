package com.blackneko.infrastructure.database

import org.flywaydb.core.Flyway

object FlywayFactory {

    fun migrate(
        config: DatabaseConfig
    ) {

        Flyway
            .configure()
            .dataSource(
                config.jdbcUrl,
                config.user,
                config.password
            )
            .locations(
                "classpath:db/migration"
            )
            .load()
            .migrate()
    }
}