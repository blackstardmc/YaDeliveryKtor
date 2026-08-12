package com.blackneko.domain.order

import java.util.UUID

interface OrderItemRepository {

    suspend fun findByOrder(
        orderId: UUID
    ): List<OrderItem>

    suspend fun save(
        item: OrderItem
    ): OrderItem

    suspend fun saveAll(
        items: List<OrderItem>
    )
}