package com.blackneko.integration

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseCleaner {

    fun clean(
        database: Database
    ) {

        transaction(database) {

            exec(
                """
                TRUNCATE TABLE
                    order_requests,
                    refresh_tokens,
                    order_status_history,
                    order_items,
                    orders,
                    drivers,
                    products,
                    categories,
                    restaurants,
                    addresses,
                    user_roles,
                    users
                RESTART IDENTITY
                CASCADE
                """.trimIndent()
            )
        }
    }
}
