package com.blackneko.integration

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MigrationIntegrationTest :
    DatabaseIntegrationTest() {

    @Test
    fun `flyway should create expected database schema`() {

        val expectedTables =
            setOf(
                "users",
                "refresh_tokens",
                "order_requests",
                "user_roles",
                "addresses",
                "restaurants",
                "categories",
                "products",
                "drivers",
                "orders",
                "order_items",
                "order_status_history"
            )

        val existingTables =
            transaction(database) {

                exec(
                    """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                    """.trimIndent()
                ) { result ->

                    buildSet {

                        while (result.next()) {
                            add(
                                result.getString(
                                    "table_name"
                                )
                            )
                        }
                    }
                } ?: emptySet()
            }

        assertTrue(
            existingTables.containsAll(
                expectedTables
            )
        )
    }

    @Test
    fun `flyway should have successful migration history`() {

        val failedCount =
            transaction(database) {

                exec(
                    """
                    SELECT COUNT(*)
                    FROM flyway_schema_history
                    WHERE success = FALSE
                    """.trimIndent()
                ) { result ->

                    result.next()
                    result.getInt(1)
                } ?: -1
            }

        assertEquals(
            0,
            failedCount
        )
    }
}
