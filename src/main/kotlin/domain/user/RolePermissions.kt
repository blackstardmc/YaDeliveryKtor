package com.blackneko.domain.user


object RolePermissions {

    private val permissions: Map<Role, Set<Permission>> =
        mapOf(

            Role.CUSTOMER to setOf(
                Permission.RESTAURANT_READ,
                Permission.PRODUCT_READ,
                Permission.CATEGORY_READ,

                Permission.ORDER_CREATE,
                Permission.ORDER_READ,
                Permission.ORDER_CANCEL
            ),

            Role.RESTAURANT to setOf(
                Permission.RESTAURANT_READ,
                Permission.RESTAURANT_UPDATE,

                Permission.PRODUCT_CREATE,
                Permission.PRODUCT_READ,
                Permission.PRODUCT_UPDATE,
                Permission.PRODUCT_DELETE,

                Permission.CATEGORY_CREATE,
                Permission.CATEGORY_READ,
                Permission.CATEGORY_UPDATE,
                Permission.CATEGORY_DELETE,

                Permission.ORDER_READ,
                Permission.ORDER_UPDATE_STATUS,

                Permission.DRIVER_READ
            ),

            Role.DRIVER to setOf(
                Permission.RESTAURANT_READ,
                Permission.PRODUCT_READ,

                Permission.ORDER_READ,
                Permission.DRIVER_READ,
                Permission.DRIVER_ACCEPT,
                Permission.DRIVER_UPDATE_LOCATION
            ),

            Role.ADMIN to Permission.entries.toSet()
        )

    fun hasPermission(
        role: Role,
        permission: Permission
    ): Boolean {
        return permissions[role]
            ?.contains(permission)
            ?: false
    }
}