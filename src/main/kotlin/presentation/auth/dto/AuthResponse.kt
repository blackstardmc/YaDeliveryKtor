package com.blackneko.presentation.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val userId: String,
    val roles: List<String>,
    val accessToken: String,
    val tokenType: String = "Bearer"
)