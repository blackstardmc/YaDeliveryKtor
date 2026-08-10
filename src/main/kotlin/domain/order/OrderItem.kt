package com.blackneko.domain.order

import com.blackneko.domain.shared.Money
import java.util.UUID

data class OrderItem(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val productName: String,
    val unitPrice: Money,
    val quantity: Int,
    val subtotal: Money
)

fun calculateSubtotal(
    unitPrice: Money,
    quantity: Int
): Money {

    require(quantity > 0)

    return Money(
        unitPrice.cents * quantity
    )
}