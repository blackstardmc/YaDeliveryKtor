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
){
    init {
        require(productName.isNotBlank()) {
            "Product name cannot be blank"
        }

        require(quantity > 0) {
            "Quantity must be greater than zero"
        }

        require(
            subtotal == unitPrice * quantity
        ) {
            "Invalid item subtotal"
        }
    }

    companion object {

        fun create(
            id: UUID,
            orderId: UUID,
            productId: UUID,
            productName: String,
            unitPrice: Money,
            quantity: Int
        ): OrderItem {

            return OrderItem(
                id = id,
                orderId = orderId,
                productId = productId,
                productName = productName,
                unitPrice = unitPrice,
                quantity = quantity,
                subtotal = unitPrice * quantity
            )
        }
    }
}

fun calculateSubtotal(
    unitPrice: Money,
    quantity: Int
): Money {

    require(quantity > 0)

    return Money(
        unitPrice.cents * quantity
    )
}