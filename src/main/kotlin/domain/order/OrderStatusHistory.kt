package com.blackneko.domain.order

import java.util.UUID
import java.time.Instant

data class OrderStatusHistory(
    val id: UUID,
    val orderId: UUID,
    val fromStatus: OrderStatus?,
    val toStatus: OrderStatus,
    val changedBy: UUID?,
    val createdAt: Instant
)