package com.blackneko.application.auth

data class LoginCommand(
    val identifier: String,
    val password: String
)