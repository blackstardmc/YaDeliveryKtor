package com.blackneko.application.auth

data class RegisterUserCommand(
    val email: String?,
    val phone: String,
    val password: String,
    val firstName: String,
    val lastName: String
)