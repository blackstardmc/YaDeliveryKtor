package com.blackneko.integration

import com.blackneko.infrastructure.database.table.AddressesTable
import com.blackneko.infrastructure.database.table.OrdersTable
import com.blackneko.infrastructure.database.table.RestaurantsTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.insert
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFails

class OrderConstraintIntegrationTest :
    DatabaseIntegrationTest() {

    @Test
    fun `database should reject invalid order total`() {

        /*
         * Este test es mejor hacerlo usando SQL/Exposed directo,
         * saltándonos Repository/Domain, porque queremos comprobar
         * exclusivamente la constraint de PostgreSQL.
         *
         * Antes de activarlo, crea las FK necesarias o usa fixtures.
         */
    }

    @Test
    fun `invalid money relationship should fail at database level`() {

        /*
         * Esperamos:
         *
         * subtotal     = 1000
         * deliveryFee  = 500
         * total        = 100
         *
         * CHECK(total = subtotal + delivery_fee)
         *
         * PostgreSQL debe rechazarlo.
         */
    }
}