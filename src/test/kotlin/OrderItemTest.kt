package com.blackneko

import com.blackneko.domain.order.OrderItem
import com.blackneko.domain.shared.Money
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.UUID
import kotlin.test.Test

class OrderItemTest {

    @Test
    fun `should calculate subtotal`() {

        val item =
            OrderItem.create(
                id = UUID.randomUUID(),
                orderId = UUID.randomUUID(),
                productId = UUID.randomUUID(),
                productName = "Pizza",
                unitPrice = Money(500),
                quantity = 3
            )

        assertEquals(
            Money(1500),
            item.subtotal
        )
    }
}