package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.category.Category
import com.blackneko.domain.category.CategoryRepository
import com.blackneko.infrastructure.database.mapper.toCategory
import com.blackneko.infrastructure.database.table.CategoriesTable
import com.blackneko.infrastructure.database.table.RestaurantsTable


import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import com.blackneko.domain.shared.PageRequest

class CategoryRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    CategoryRepository {

    override suspend fun findById(
        id: UUID
    ): Category? =
        transactionRunner.transaction {

            CategoriesTable
                .selectAll()
                .where {
                    CategoriesTable.id eq id
                }
                .singleOrNull()
                ?.toCategory()
        }

    override suspend fun findByRestaurant(
        restaurantId: UUID, page: PageRequest, activeOnly: Boolean): List<Category> =
        transactionRunner.transaction {

            CategoriesTable
                .selectAll()
                .where {
                    (CategoriesTable.restaurant eq
                            EntityID(
                                restaurantId,
                                RestaurantsTable
                            )) and (if (activeOnly) CategoriesTable.isActive eq true else Op.TRUE)
                }
                .orderBy(CategoriesTable.sortOrder to SortOrder.ASC, CategoriesTable.id to SortOrder.ASC)
                .limit(page.limit).offset(page.offset.toLong())
                .map {
                    it.toCategory()
                }
        }



    override suspend fun save(
        category: Category
    ): Category =
        transactionRunner.transaction {

            CategoriesTable.insert {

                it[id] =
                    category.id

                it[restaurant] =
                    EntityID(
                        category.restaurantId,
                        RestaurantsTable
                    )

                it[name] =
                    category.name

                it[description] =
                    category.description

                it[sortOrder] =
                    category.sortOrder

                it[isActive] =
                    category.isActive

                it[createdAt] =
                    category.createdAt

                it[updatedAt] =
                    category.updatedAt
            }

            category
        }

    override suspend fun update(
        category: Category
    ): Category =
        transactionRunner.transaction {

            CategoriesTable.update(
                where = {
                    CategoriesTable.id eq
                            category.id
                }
            ) {

                it[restaurant] =
                    EntityID(
                        category.restaurantId,
                        RestaurantsTable
                    )

                it[name] =
                    category.name

                it[description] =
                    category.description

                it[sortOrder] =
                    category.sortOrder

                it[isActive] =
                    category.isActive

                it[updatedAt] =
                    category.updatedAt
            }

            category
        }

    override suspend fun delete(
        id: UUID
    ) {
        transactionRunner.transaction {

            CategoriesTable.deleteWhere {
                CategoriesTable.id eq id
            }
        }
    }
}
