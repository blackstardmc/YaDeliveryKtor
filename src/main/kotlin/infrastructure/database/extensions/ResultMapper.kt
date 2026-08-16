package com.blackneko.infrastructure.database.extensions


import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.Query

fun <T> Query.mapRows(
    mapper: (ResultRow) -> T
): List<T> {

    return map(mapper)
}