package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object AddressesTable : UUIDTable("addresses") {

    val user =
        reference(
            "user_id",
            UsersTable,
            onDelete = ReferenceOption.CASCADE
        )

    val label = varchar("label", 100).nullable()

    val street = varchar("street", 255)

    val number = varchar("number", 30).nullable()

    val neighborhood =
        varchar(
            "neighborhood",
            100
        ).nullable()

    val city = varchar("city", 100)

    val province =
        varchar(
            "province",
            100
        ).nullable()

    val referenceText =
        text("reference")
            .nullable()

    val latitude =
        double("latitude")
            .nullable()

    val longitude =
        double("longitude")
            .nullable()

    val isDefault =
        bool("is_default")

    val createdAt =
        timestamp("created_at")

    val updatedAt =
        timestamp("updated_at")
}