package com.blackneko.domain.order

import com.blackneko.domain.shared.Money
import java.util.UUID
import kotlin.time.Instant

data class Order(
    val id: UUID,
    val customerId: UUID,
    val restaurantId: UUID,
    val driverId: UUID?,
    val deliveryAddressId: UUID,
    val status: OrderStatus,
    val subtotal: Money,
    val deliveryFee: Money,
    val total: Money,
    val notes: String?,
    val createdAt: Instant,
    val confirmedAt: Instant?,
    val preparingAt: Instant?,
    val readyAt: Instant?,
    val pickedUpAt: Instant?,
    val deliveredAt: Instant?,
    val cancelledAt: Instant?,
    val updatedAt: Instant
)

