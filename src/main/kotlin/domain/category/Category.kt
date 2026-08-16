package com.blackneko.domain.category

import java.util.UUID
import java.time.Instant

data class Category(
    val id: UUID,
    val restaurantId: UUID,
    val name: String,
    val description: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
){
    init {
        require(name.isNotBlank()) {
            "Category name cannot be blank"
        }

        require(sortOrder >= 0) {
            "Sort order cannot be negative"
        }
    }
}