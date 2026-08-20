package com.blackneko.domain.product

import java.util.UUID

data class ProductFilter(
    val restaurantId: UUID,
    val categoryId: UUID? = null,
    val available: Boolean? = null,
    val search: String? = null
)