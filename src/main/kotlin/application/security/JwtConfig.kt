package com.blackneko.application.security

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val accessTokenExpirationMinutes: Long
)