package com.blackneko.application.marketplace

import com.blackneko.application.exception.*
import com.blackneko.domain.security.Permission
import com.blackneko.domain.user.*
import com.blackneko.domain.restaurant.*
import java.util.UUID

class AccessPolicy(private val users: UserRepository, private val restaurants: RestaurantRepository) {
    suspend fun contactPhone(userId: UUID): String? = users.findById(userId)?.phone
    suspend fun actor(id: UUID): User {
        val user = users.findById(id) ?: throw AuthenticationException()
        if (!user.isActive) throw AuthenticationException()
        return user
    }

    suspend fun require(id: UUID, permission: Permission): User {
        val user = actor(id)
        if (!user.hasPermission(permission)) throw AuthorizationException()
        return user
    }

    suspend fun restaurant(id: UUID, restaurantId: UUID, permission: Permission): Restaurant {
        val actor = require(id, permission)
        val restaurant = restaurants.findById(restaurantId) ?: throw NotFoundException("Restaurant not found")
        if (restaurant.ownerId != actor.id && !actor.hasRole(Role.ADMIN)) throw AuthorizationException()
        return restaurant
    }

    fun own(actorId: UUID, ownerId: UUID) {
        if (actorId != ownerId) throw AuthorizationException()
    }
}

internal fun validate(valid: Boolean, field: String, message: String) {
    if (!valid) throw ValidationException(message, field)
}

internal fun text(value: String, field: String, maximum: Int): String {
    val trimmed = value.trim()
    validate(trimmed.length in 1..maximum, field, "Invalid $field")
    return trimmed
}

internal fun optionalText(value: String?, field: String, maximum: Int): String? {
    validate(value == null || value.length <= maximum, field, "$field is too long")
    return value?.trim()?.takeIf { it.isNotEmpty() }
}
