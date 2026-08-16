package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.product.Product
import com.blackneko.domain.shared.Money
import com.blackneko.infrastructure.database.table.ProductsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

fun ResultRow.toProduct(): Product =
    Product(
        id = this[ProductsTable.id].value,
        restaurantId = this[ProductsTable.restaurant].value,
        categoryId = this[ProductsTable.category]?.value,
        name = this[ProductsTable.name],
        description = this[ProductsTable.description],
        price = Money(this[ProductsTable.price]),
        isAvailable = this[ProductsTable.isAvailable],
        createdAt = this[ProductsTable.createdAt],
        updatedAt = this[ProductsTable.updatedAt]
    )

fun UpdateBuilder<*>.fromProduct(product: Product) {

    this[ProductsTable.id] = product.id

    this[ProductsTable.restaurant] = product.restaurantId

    this[ProductsTable.category] = product.categoryId

    this[ProductsTable.name] = product.name

    this[ProductsTable.description] = product.description

    this[ProductsTable.price] = product.price.cents

    this[ProductsTable.isAvailable] = product.isAvailable

    this[ProductsTable.createdAt] = product.createdAt

    this[ProductsTable.updatedAt] = product.updatedAt
}