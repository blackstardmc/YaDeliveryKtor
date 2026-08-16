package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.restaurant.Restaurant
import com.blackneko.domain.restaurant.RestaurantStatus
import com.blackneko.infrastructure.database.table.RestaurantsTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toRestaurant() =
    Restaurant(

        id = this[RestaurantsTable.id].value,

        ownerId =
            this[RestaurantsTable.owner].value,

        addressId =
            this[RestaurantsTable.address].value,

        name =
            this[RestaurantsTable.name],

        description =
            this[RestaurantsTable.description],

        phone =
            this[RestaurantsTable.phone],

        status =
            RestaurantStatus.valueOf(
                this[RestaurantsTable.status]
            ),

        createdAt =
            this[RestaurantsTable.createdAt],

        updatedAt =
            this[RestaurantsTable.updatedAt]
    )