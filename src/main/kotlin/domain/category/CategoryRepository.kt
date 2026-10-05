package com.blackneko.domain.category


import java.util.UUID
import com.blackneko.domain.shared.PageRequest

interface CategoryRepository {

    suspend fun findById(
        id: UUID
    ): Category?

    suspend fun findByRestaurant(
        restaurantId: UUID, page: PageRequest = PageRequest(), activeOnly: Boolean = false): List<Category>

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
