package com.blackneko.infrastructure.security

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val accessTokenExpirationMinutes: Long,
    val refreshTokenExpirationDays: Long
) {
    init {
        require(secret.toByteArray(Charsets.UTF_8).size >= 32) { "JWT secret must contain at least 32 bytes" }
        require(issuer.isNotBlank() && audience.isNotBlank() && realm.isNotBlank()) { "JWT identity configuration is required" }
        require(accessTokenExpirationMinutes in 1..1440 && refreshTokenExpirationDays in 1..365) { "Invalid token expiration" }
    }
}
