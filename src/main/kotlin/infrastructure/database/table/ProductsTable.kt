package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object ProductsTable :

    UUIDTable(
        name = "products"
    ) {

    val restaurant =
        reference(
            name = "restaurant_id",
            foreign = RestaurantsTable,
            onDelete = ReferenceOption.CASCADE
        )

    val category =
        optReference(
            name = "category_id",
            foreign = CategoriesTable,
            onDelete = ReferenceOption.SET_NULL
        )

    val name =
        varchar(
            name = "name",
            length = 200
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