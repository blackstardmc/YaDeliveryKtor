package com.blackneko.security

import com.blackneko.domain.security.Permission
import com.blackneko.domain.security.RolePermissions
import com.blackneko.domain.user.Role
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RolePermissionsTest {

    @Test
    fun `customer should create orders`() {

        val permissions =
            RolePermissions.permissionsFor(
                setOf(
                    Role.CUSTOMER
                )
            )

        assertTrue(
            Permission.ORDER_CREATE
                    in permissions
        )
    }

    @Test
    fun `driver should not create customer order`() {

        val permissions =
            RolePermissions.permissionsFor(
                setOf(
                    Role.DRIVER
                )
            )

        assertFalse(
            Permission.ORDER_CREATE
                    in permissions
        )
    }

    @Test
    fun `admin should contain every permission`() {

        val permissions =
            RolePermissions.permissionsFor(
                setOf(
                    Role.ADMIN
                )
            )

        assertTrue(
            Permission.entries
                .all {
                    it in permissions
                }
        )
    }

    @Test
    fun `multiple roles should combine permissions`() {

        val permissions =
            RolePermissions.permissionsFor(
                setOf(
                    Role.CUSTOMER,
                    Role.DRIVER
                )
            )

        assertTrue(
            Permission.ORDER_CREATE
                    in permissions
        )

        assertTrue(
            Permission.DRIVER_ORDER_ACCEPT
                    in permissions
        )
    }
}