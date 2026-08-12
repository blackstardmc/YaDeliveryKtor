package com.blackneko

import com.blackneko.domain.order.OrderFactory
import com.blackneko.domain.order.OrderStatus
import com.blackneko.domain.shared.Money
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.UUID
import kotlin.test.Test

class OrderTest {

    @Test
    fun `received order can be cancelled`() {

        val order =
            OrderFactory.create(
                id = UUID.randomUUID(),
                customerId = UUID.randomUUID(),
                restaurantId = UUID.randomUUID(),
                deliveryAddressId = UUID.randomUUID(),
                subtotal = Money(1000),
                deliveryFee = Money(200),
                notes = null
            )

        val cancelled =
            order.cancel()

        kotlin.test.assertEquals(
            OrderStatus.CANCELLED,
            cancelled.status
        )
    }

    @Test
    fun `should follow normal lifecycle`() {

        val order =
            OrderFactory.create(
                id = UUID.randomUUID(),
                customerId = UUID.randomUUID(),
                restaurantId = UUID.randomUUID(),
                deliveryAddressId = UUID.randomUUID(),
                subtotal = Money(1000),
                deliveryFee = Money(200),
                notes = null
            )

        val confirmed =
            order.transitionTo(
                OrderStatus.CONFIRMED
            )

        val preparing =
            confirmed.transitionTo(
                OrderStatus.PREPARING
            )

        val ready =
            preparing.transitionTo(
                OrderStatus.READY
            )

        val inDelivery =
            ready.transitionTo(
                OrderStatus.IN_DELIVERY
            )

        val delivered =
            inDelivery.transitionTo(
                OrderStatus.DELIVERED
            )

        assertEquals(
            OrderStatus.DELIVERED,
            delivered.status
        )

        assertEquals(
            Money(1200),
            delivered.total
        )
    }
}

