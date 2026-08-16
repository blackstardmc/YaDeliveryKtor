package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

object OrderItemsTable : UUIDTable("order_items") {

    val order =
        reference(
            "order_id",
            OrdersTable
        )

    val product =
        reference(
            "product_id",
            ProductsTable
        )

    val productName =
        varchar(
            "product_name",
            200
        )

    val unitPrice =
        long("unit_price")

    val quantity =
        integer("quantity")

    val subtotal =
        long("subtotal")
}