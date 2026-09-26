package com.blackneko.presentation.security

import com.blackneko.application.exception.AuthorizationException
import com.blackneko.domain.security.Permission
import com.blackneko.domain.security.RolePermissions
import io.ktor.server.application.*

fun ApplicationCall.requirePermission(
    permission: Permission
) {

    val authenticated =
        authenticatedUser()

    val permissions =
        RolePermissions.permissionsFor(
            authenticated.roles
        )

    if (
        permission !in permissions
    ) {

        throw AuthorizationException()
    }
}