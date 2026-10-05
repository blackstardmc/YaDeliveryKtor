package com.blackneko.application.auth

import com.blackneko.application.exception.ValidationException

object RegisterUserValidator {
    fun validate(command: RegisterUserCommand) {
        fun check(valid: Boolean, field: String, message: String) {
            if (!valid) throw ValidationException(message, field)
        }
        check(command.phone.trim().length in 1..20, "phone", "Phone is required and must not exceed 20 characters")
        check(command.firstName.trim().length in 1..100, "firstName", "First name is required and must not exceed 100 characters")
        check(command.lastName.trim().length in 1..100, "lastName", "Last name is required and must not exceed 100 characters")
        check(command.password.length >= 8 && command.password.toByteArray(Charsets.UTF_8).size <= 72,
            "password", "Password must contain at least 8 characters and at most 72 UTF-8 bytes")
        command.email?.let {
            check(it.trim().length <= 255 && Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(it.trim()), "email", "Invalid email")
        }
    }
}

object LoginValidator {
    fun validate(command: LoginCommand) {
        if (command.identifier.isBlank() || command.identifier.length > 255)
            throw ValidationException("Identifier is required and must not exceed 255 characters", "identifier")
        if (command.password.isEmpty() || command.password.toByteArray(Charsets.UTF_8).size > 72)
            throw ValidationException("Password is required and must not exceed 72 UTF-8 bytes", "password")
    }
}
