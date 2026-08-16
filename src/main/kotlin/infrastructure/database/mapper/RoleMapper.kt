package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.user.Role
import com.blackneko.infrastructure.database.table.CategoriesTable.name

fun String.toRole() =
    Role.valueOf(this)

fun Role.toDatabase() =
    name