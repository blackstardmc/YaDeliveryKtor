package com.blackneko.infrastructure.database

import com.blackneko.application.ReadinessProbe
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

class DatabaseReadinessProbe(private val dataSource: HikariDataSource) : ReadinessProbe {
    override suspend fun isReady(): Boolean = withContext(Dispatchers.IO) {
        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.queryTimeout = 2
                    statement.executeQuery("SELECT 1").use { it.next() && it.getInt(1) == 1 }
                }
            }
        } catch (_: SQLException) { false }
    }
}
