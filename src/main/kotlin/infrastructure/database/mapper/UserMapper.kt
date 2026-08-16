package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toUser(
    roles: Set<Role>
): User {

    return User(

        id = this[UsersTable.id].value,

        email = this[UsersTable.email],

        phone = this[UsersTable.phone],

        passwordHash = this[UsersTable.passwordHash],

        firstName = this[UsersTable.firstName],

        lastName = this[UsersTable.lastName],

        roles = roles,

        isActive = this[UsersTable.isActive],

        createdAt = this[UsersTable.createdAt],

        updatedAt = this[UsersTable.updatedAt]
    )
}