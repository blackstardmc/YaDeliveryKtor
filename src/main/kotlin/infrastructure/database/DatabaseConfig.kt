package com.blackneko.infrastructure.database

data class DatabaseConfig(

    val host: String,

    val port: Int,

    val database: String,

    val user: String,

    val password: String
) {

    val jdbcUrl: String
        get() =
            "jdbc:postgresql://$host:$port/$database"
}