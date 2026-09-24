package com.blackneko.application.exception

sealed class ApplicationException(
    message: String
) : RuntimeException(message)