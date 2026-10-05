package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.restaurant.Restaurant
import com.blackneko.domain.restaurant.RestaurantRepository
import com.blackneko.domain.restaurant.RestaurantStatus
import com.blackneko.infrastructure.database.mapper.toRestaurant
import com.blackneko.infrastructure.database.table.AddressesTable
import com.blackneko.infrastructure.database.table.RestaurantsTable
import com.blackneko.infrastructure.database.table.UsersTable


import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import com.blackneko.domain.shared.PageRequest

class RestaurantRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    RestaurantRepository {

    override suspend fun findById(
        id: UUID
    ): Restaurant? =
        transactionRunner.transaction {

            RestaurantsTable
                .selectAll()
                .where {
                    RestaurantsTable.id eq id
                }
                .singleOrNull()
                ?.toRestaurant()
        }

    override suspend fun findByOwner(
        ownerId: UUID, page: PageRequest): List<Restaurant> =
        transactionRunner.transaction {

            RestaurantsTable
                .selectAll()
                .where {
                    RestaurantsTable.owner eq
                            EntityID(
                                ownerId,
                                UsersTable
                            )
                }
                .limit(page.limit).offset(page.offset.toLong())
                .orderBy(RestaurantsTable.id, org.jetbrains.exposed.v1.core.SortOrder.ASC)
                .map {
                    it.toRestaurant()
                }
        }

    override suspend fun findAllActive(page: PageRequest): List<Restaurant> =
        transactionRunner.transaction {

            RestaurantsTable
                .selectAll()
                .where {
                    RestaurantsTable.status eq
                            RestaurantStatus.ACTIVE.name
                }
                .limit(page.limit).offset(page.offset.toLong())
                .orderBy(RestaurantsTable.id, org.jetbrains.exposed.v1.core.SortOrder.ASC)
                .map {
                    it.toRestaurant()
                }
        }

    override suspend fun save(
        restaurant: Restaurant
    ): Restaurant =
        transactionRunner.transaction {

            RestaurantsTable.insert {

                it[id] = restaurant.id

                it[owner] =
                    EntityID(
                        restaurant.ownerId,
                        UsersTable
                    )

                it[address] =
                    EntityID(
                        restaurant.addressId,
                        AddressesTable
                    )

                it[name] =
                    restaurant.name

                it[description] =
                    restaurant.description

                it[phone] =
                    restaurant.phone

                it[status] =
                    restaurant.status.name

                it[createdAt] =
                    restaurant.createdAt

                it[updatedAt] =
                    restaurant.updatedAt
            }

            restaurant
        }

    override suspend fun update(
        restaurant: Restaurant
    ): Restaurant =
        transactionRunner.transaction {

            RestaurantsTable.update(
                where = {
                    RestaurantsTable.id eq
                            restaurant.id
                }
            ) {

                it[owner] =
                    EntityID(
                        restaurant.ownerId,
                        UsersTable
                    )

                it[address] =
                    EntityID(
                        restaurant.addressId,
                        AddressesTable
                    )

                it[name] =
                    restaurant.name

                it[description] =
                    restaurant.description

                it[phone] =
                    restaurant.phone

                it[status] =
                    restaurant.status.name

                it[updatedAt] =
                    restaurant.updatedAt
            }

            restaurant
        }

    override suspend fun delete(
        id: UUID
    ) {
        transactionRunner.transaction {

            RestaurantsTable.deleteWhere {
                RestaurantsTable.id eq id
            }
        }
    }
}
