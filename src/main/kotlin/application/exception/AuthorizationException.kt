package com.blackneko.application.exception

class AuthorizationException(
    message: String = "Access denied"
) : ApplicationException(message)