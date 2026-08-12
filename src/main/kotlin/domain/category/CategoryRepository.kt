package com.blackneko.domain.category


import java.util.UUID

interface CategoryRepository {

    suspend fun findById(
        id: UUID
    ): Category?

    suspend fun findByRestaurant(
        restaurantId: UUID
    ): List<Category>

    suspend fun save(
        category: Category
    ): Category

    suspend fun update(
        category: Category
    ): Category

    suspend fun delete(
        id: UUID
    )
}
