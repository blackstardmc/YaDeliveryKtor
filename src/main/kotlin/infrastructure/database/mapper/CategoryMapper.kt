package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.category.Category
import com.blackneko.infrastructure.database.table.CategoriesTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toCategory() =
    Category(

        id =
            this[CategoriesTable.id].value,

        restaurantId =
            this[CategoriesTable.restaurant].value,

        name =
            this[CategoriesTable.name],

        description =
            this[CategoriesTable.description],

        sortOrder =
            this[CategoriesTable.sortOrder],

        isActive =
            this[CategoriesTable.isActive],

        createdAt =
            this[CategoriesTable.createdAt],

        updatedAt =
            this[CategoriesTable.updatedAt]
    )