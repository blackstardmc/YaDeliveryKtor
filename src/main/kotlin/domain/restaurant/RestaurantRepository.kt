package com.blackneko.domain.restaurant

import java.util.UUID

interface RestaurantRepository {

    suspend fun findById(id: UUID): Restaurant?

    suspend fun findAll(): List<Restaurant>

    suspend fun save(restaurant: Restaurant): Restaurant

    suspend fun update(restaurant: Restaurant): Restaurant

    suspend fun delete(id: UUID)
}