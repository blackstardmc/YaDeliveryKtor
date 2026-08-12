package com.blackneko

import com.blackneko.domain.order.OrderStatus
import com.blackneko.domain.order.OrderStatusTransitions
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test
import kotlin.test.assertTrue

class OrderStatusTransitionsTest {

    @Test
    fun `received can become confirmed`() {

        assertTrue(
            OrderStatusTransitions.canTransition(
                OrderStatus.RECEIVED,
                OrderStatus.CONFIRMED
            )
        )
    }

    @Test
    fun `delivered cannot become preparing`() {

        assertFalse(
            OrderStatusTransitions.canTransition(
                OrderStatus.DELIVERED,
                OrderStatus.PREPARING
            )
        )
    }

    @Test
    fun `cancelled cannot become confirmed`() {

        assertFalse(
            OrderStatusTransitions.canTransition(
                OrderStatus.CANCELLED,
                OrderStatus.CONFIRMED
            )
        )
    }
}