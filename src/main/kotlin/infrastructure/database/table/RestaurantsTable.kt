package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object RestaurantsTable : UUIDTable("restaurants") {

    val owner =
        reference(
            "owner_id",
            UsersTable
        )

    val address =
        reference(
            "address_id",
            AddressesTable
        )

    val name =
        varchar(
            "name",
            200
        )

    val description =
        text("description")
            .nullable()

    val phone =
        varchar(
            "phone",
            20
        ).nullable()

    val status =
        varchar(
            "status",
            30
        )

    val createdAt =
        timestamp("created_at")

    val updatedAt =
        timestamp("updated_at")
}