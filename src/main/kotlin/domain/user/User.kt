package com.blackneko.domain.user

import java.time.Instant
import java.util.UUID
import com.blackneko.domain.security.Permission
import com.blackneko.domain.security.RolePermissions

data class User(
    val id: UUID,
    val email: String?,
    val phone: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val roles: Set<Role>,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(phone.isNotBlank()) {
            "Phone cannot be blank"
        }

        require(firstName.isNotBlank()) {
            "First name cannot be blank"
        }

        require(lastName.isNotBlank()) {
            "Last name cannot be blank"
        }

        require(roles.isNotEmpty()) {
            "User must have at least one role"
        }
    }

    fun hasRole(role: Role): Boolean {
        return role in roles
    }

    fun hasPermission(
        permission: Permission
    ): Boolean {
        return permission in RolePermissions.permissionsFor(roles)
    }

    fun canLogin(): Boolean {
        return isActive
    }
}



