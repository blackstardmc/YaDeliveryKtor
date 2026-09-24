package com.blackneko.application.exception

class AuthenticationException(
    message: String = "Invalid credentials"
) : ApplicationException(message)