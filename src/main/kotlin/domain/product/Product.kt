package com.blackneko.domain.product

import com.blackneko.domain.shared.Money
import java.util.UUID
import kotlin.time.Instant

data class Product(
    val id: UUID,
    val restaurantId: UUID,
    val categoryId: UUID?,
    val name: String,
    val description: String?,
    val price: Money,
    val isAvailable: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) {
            "Product name cannot be blank"
        }
    }

    fun canBeOrdered(): Boolean {
        return isAvailable &&
                !price.isZero()
    }
}