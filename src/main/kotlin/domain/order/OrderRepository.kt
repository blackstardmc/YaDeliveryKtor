package com.blackneko.domain.order

import java.util.UUID
import com.blackneko.domain.shared.PageRequest

interface OrderRepository {

    suspend fun findById(
        id: UUID
    ): Order?

    suspend fun findByCustomer(
        customerId: UUID, page: PageRequest = PageRequest()): List<Order>

    suspend fun findByRestaurant(
        restaurantId: UUID, page: PageRequest = PageRequest()): List<Order>

    suspend fun findByDriver(
        driverId: UUID, page: PageRequest = PageRequest()): List<Order>

    suspend fun findAvailableForDrivers(page: PageRequest = PageRequest()): List<Order>

    suspend fun save(
        order: Order
    ): Order

    suspend fun update(
        order: Order
    ): Order
}
