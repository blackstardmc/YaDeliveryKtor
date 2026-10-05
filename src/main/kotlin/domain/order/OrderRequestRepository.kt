package com.blackneko.domain.order

import java.util.UUID

data class OrderRequest(val userId: UUID, val key: String, val fingerprint: String, val orderId: UUID)

interface OrderRequestRepository {
    suspend fun find(userId: UUID, key: String): OrderRequest?
    suspend fun save(request: OrderRequest)
}
