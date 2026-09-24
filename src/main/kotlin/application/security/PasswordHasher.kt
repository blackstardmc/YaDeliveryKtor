package com.blackneko.application.security

interface PasswordHasher {

    fun hash(
        password: String
    ): String

    fun verify(
        password: String,
        hash: String
    ): Boolean
}