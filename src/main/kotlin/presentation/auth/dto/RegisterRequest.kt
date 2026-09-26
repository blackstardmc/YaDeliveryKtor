package com.blackneko.presentation.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String? = null,
    val phone: String,
    val password: String,
    val firstName: String,
    val lastName: String
)