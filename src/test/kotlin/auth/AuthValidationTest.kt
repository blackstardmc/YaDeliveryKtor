package com.blackneko.auth

import com.blackneko.application.auth.*
import com.blackneko.application.exception.ValidationException
import org.junit.jupiter.api.Test
import kotlin.test.*

class AuthValidationTest {
    private val valid = RegisterUserCommand("test@example.com", "+5355551234", "Password123!", "Ana", "Perez")

    @Test
    fun `valid registration and optional email accepted`() {
        RegisterUserValidator.validate(valid)
        RegisterUserValidator.validate(valid.copy(email = null))
    }

    @Test
    fun `invalid registration reports field`() {
        listOf(
            "email" to valid.copy(email = "bad-email"),
            "phone" to valid.copy(phone = " "),
            "firstName" to valid.copy(firstName = " "),
            "lastName" to valid.copy(lastName = " "),
            "password" to valid.copy(password = "short"),
            "password" to valid.copy(password = "é".repeat(37)),
            "phone" to valid.copy(phone = "1".repeat(21)),
            "firstName" to valid.copy(firstName = "a".repeat(101))
        ).forEach { (field, command) ->
            assertEquals(field, assertFailsWith<ValidationException> { RegisterUserValidator.validate(command) }.field)
        }
    }

    @Test
    fun `login validator accepts identifier and rejects empty input`() {
        LoginValidator.validate(LoginCommand("test@example.com", "Password123!"))
        LoginValidator.validate(LoginCommand("+5355551234", "Password123!"))
        assertEquals("identifier", assertFailsWith<ValidationException> { LoginValidator.validate(LoginCommand(" ", "password")) }.field)
        assertEquals("password", assertFailsWith<ValidationException> { LoginValidator.validate(LoginCommand("ana", "")) }.field)
    }
}
