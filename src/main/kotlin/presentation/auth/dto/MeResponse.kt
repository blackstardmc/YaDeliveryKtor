package com.blackneko.presentation.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class MeResponse(
    val id: String,
    val email: String?,
    val phone: String,
    val firstName: String,
    val lastName: String,
    val roles: List<String>,
    val isActive: Boolean
)