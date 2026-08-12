package com.blackneko.domain.order

import java.util.UUID

interface OrderStatusHistoryRepository {

    suspend fun save(
        history: OrderStatusHistory
    ): OrderStatusHistory

    suspend fun findByOrder(
        orderId: UUID
    ): List<OrderStatusHistory>
}