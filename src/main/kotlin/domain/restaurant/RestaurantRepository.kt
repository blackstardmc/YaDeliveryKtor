package com.blackneko.domain.restaurant

import java.util.UUID
import com.blackneko.domain.shared.PageRequest

interface RestaurantRepository {

    suspend fun findById(
        id: UUID
    ): Restaurant?

    suspend fun findByOwner(
        ownerId: UUID, page: PageRequest = PageRequest()): List<Restaurant>

    suspend fun findAllActive(page: PageRequest = PageRequest()): List<Restaurant>

    suspend fun save(
        restaurant: Restaurant
    ): Restaurant

    suspend fun update(
        restaurant: Restaurant
    ): Restaurant

    suspend fun delete(
        id: UUID
    )
}
