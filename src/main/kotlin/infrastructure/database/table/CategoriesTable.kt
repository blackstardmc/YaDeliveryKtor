package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object CategoriesTable : UUIDTable("categories") {

    val restaurant =
        reference(
            "restaurant_id",
            RestaurantsTable
        )

    val name =
        varchar("name", 120)

    val description =
        text("description")
            .nullable()

    val sortOrder =
        integer("sort_order")

    val isActive =
        bool("is_active")

    val createdAt =
        timestamp("created_at")

    val updatedAt =
        timestamp("updated_at")
}