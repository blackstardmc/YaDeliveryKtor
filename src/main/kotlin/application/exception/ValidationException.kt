package com.blackneko.application.exception

class ValidationException(message: String, val field: String? = null) : ApplicationException(message) {
}
