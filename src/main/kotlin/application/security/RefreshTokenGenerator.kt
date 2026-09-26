package com.blackneko.application.security

interface RefreshTokenGenerator {

    fun generate(): String

    fun hash(
        token: String
    ): String
}