package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object OrdersTable : UUIDTable("orders") {

    val customer =
        reference(
            "customer_id",
            UsersTable
        )

    val restaurant =
        reference(
            "restaurant_id",
            RestaurantsTable
        )

    val driver =
        optReference(
            "driver_id",
            DriversTable
        )

    val deliveryAddress =
        reference(
            "delivery_address_id",
            AddressesTable
        )

    val status =
        varchar(
            "status",
            30
        )

    val subtotal =
        long("subtotal")

    val deliveryFee =
        long("delivery_fee")

    val total =
        long("total")

    val notes =
        text("notes")
            .nullable()

    val createdAt =
        timestamp("created_at")

    val confirmedAt =
        timestamp("confirmed_at")
            .nullable()

    val preparingAt =
        timestamp("preparing_at")
            .nullable()

    val readyAt =
        timestamp("ready_at")
            .nullable()

    val pickedUpAt =
        timestamp("picked_up_at")
            .nullable()

    val deliveredAt =
        timestamp("delivered_at")
            .nullable()

    val cancelledAt =
        timestamp("cancelled_at")
            .nullable()

    val updatedAt =
        timestamp("updated_at")
}