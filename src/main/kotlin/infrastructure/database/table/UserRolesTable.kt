package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table


object UserRolesTable : Table("user_roles") {

    val user =
        reference(
            "user_id",
            UsersTable
        )

    val role =
        varchar(
            "role",
            30
        )

    override val primaryKey =
        PrimaryKey(
            user,
            role
        )
}