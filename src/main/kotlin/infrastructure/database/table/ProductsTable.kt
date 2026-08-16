package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object ProductsTable : UUIDTable("products") {

    val restaurant =
        reference(
            "restaurant_id",
            RestaurantsTable
        )

    val category =
        optReference(
            "category_id",
            CategoriesTable
        )

    val name =
        varchar(
            "name",
            200
        )

    val description =
        text("description")
            .nullable()

    val price =
        long("price")

    val isAvailable =
        bool("is_available")

    val createdAt =
        timestamp("created_at")

    val updatedAt =
        timestamp("updated_at")
}