package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.address.Address
import com.blackneko.infrastructure.database.table.AddressesTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toAddress() =
    Address(

        id =
            this[AddressesTable.id].value,

        userId =
            this[AddressesTable.user].value,

        label =
            this[AddressesTable.label],

        street =
            this[AddressesTable.street],

        number =
            this[AddressesTable.number],

        neighborhood =
            this[AddressesTable.neighborhood],

        city =
            this[AddressesTable.city],

        province =
            this[AddressesTable.province],

        reference =
            this[AddressesTable.referenceText],

        latitude =
            this[AddressesTable.latitude],

        longitude =
            this[AddressesTable.longitude],

        isDefault =
            this[AddressesTable.isDefault],

        createdAt =
            this[AddressesTable.createdAt],

        updatedAt =
            this[AddressesTable.updatedAt]
    )