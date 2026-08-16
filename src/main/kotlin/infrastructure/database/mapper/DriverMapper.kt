package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.driver.Driver
import com.blackneko.domain.driver.DriverStatus
import com.blackneko.domain.driver.VehicleType
import com.blackneko.infrastructure.database.table.DriversTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toDriver() =
    Driver(

        id =
            this[DriversTable.id].value,

        userId =
            this[DriversTable.user].value,

        status =
            DriverStatus.valueOf(
                this[DriversTable.status]
            ),

        vehicleType =
            VehicleType.valueOf(this[DriversTable.vehicleType]?:"") ,

        vehicleDescription =
            this[DriversTable.vehicleDescription],

        createdAt =
            this[DriversTable.createdAt],

        updatedAt =
            this[DriversTable.updatedAt]
    )