package com.blackneko.domain.security

import com.blackneko.domain.user.Role

object RolePermissions {

    private val permissions =
        mapOf(

            Role.CUSTOMER to
                    setOf(
                        Permission.PROFILE_READ,
                        Permission.ADDRESS_MANAGE,
                        Permission.ORDER_CREATE,
                        Permission.ORDER_READ_OWN,
                        Permission.ORDER_CANCEL_OWN
                    ),

            Role.RESTAURANT to
                    setOf(
                        Permission.PROFILE_READ,

                        Permission.RESTAURANT_UPDATE,
                        Permission.RESTAURANT_CREATE,
                        Permission.ADDRESS_MANAGE,
                        Permission.CATEGORY_MANAGE,

                        Permission.PRODUCT_CREATE,
                        Permission.PRODUCT_UPDATE,
                        Permission.PRODUCT_DELETE,

                        Permission.RESTAURANT_ORDER_READ,
                        Permission.RESTAURANT_ORDER_UPDATE
                    ),

            Role.DRIVER to
                    setOf(
                        Permission.PROFILE_READ,

                        Permission.DRIVER_ORDER_LIST,
                        Permission.DRIVER_ORDER_ACCEPT,
                        Permission.DRIVER_ORDER_UPDATE
                    ),

            Role.ADMIN to
                    Permission.entries.toSet()
        )

    fun permissionsFor(
        roles: Set<Role>
    ): Set<Permission> {

        return roles
            .flatMap { role ->
                permissions[role].orEmpty()
            }
            .toSet()
    }
}
