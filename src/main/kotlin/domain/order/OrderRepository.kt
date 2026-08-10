package com.blackneko.domain.order

import java.util.UUID

interface OrderRepository {

    suspend fun findById(id: UUID): Order?

    suspend fun findByCustomer(
        customerId: UUID
    ): List<Order>

    suspend fun findByRestaurant(
        restaurantId: UUID
    ): List<Order>

    suspend fun findByDriver(
        driverId: UUID
    ): List<Order>

    suspend fun save(order: Order): Order

    suspend fun update(order: Order): Order
}