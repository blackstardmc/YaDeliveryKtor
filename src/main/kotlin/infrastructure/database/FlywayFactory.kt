package com.blackneko.infrastructure.database

import org.flywaydb.core.Flyway
import javax.sql.DataSource

object FlywayFactory {

    fun migrate(
        dataSource: DataSource
    ) {

        Flyway
            .configure()
            .dataSource(
                dataSource
            )
            .locations(
                "classpath:db/migration"
            )
            .baselineOnMigrate(false)
            .validateOnMigrate(true)
            .load()
            .migrate()
    }
}