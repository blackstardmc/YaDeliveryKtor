package com.blackneko.domain.product

import java.util.UUID

interface ProductRepository {

    suspend fun findById(
        id: UUID
    ): Product?

    suspend fun findByRestaurant(
        restaurantId: UUID
    ): List<Product>

    suspend fun findByCategory(
        categoryId: UUID
    ): List<Product>

    suspend fun findAvailableByRestaurant(
        restaurantId: UUID
    ): List<Product>

    suspend fun search(
        filter: ProductFilter
    ): List<Product>

    suspend fun save(
        product: Product
    ): Product

    suspend fun update(
        product: Product
    ): Product

    suspend fun delete(
        id: UUID
    )
}