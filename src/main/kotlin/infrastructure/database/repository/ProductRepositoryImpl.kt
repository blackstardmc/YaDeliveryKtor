package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.product.Product
import com.blackneko.domain.product.ProductFilter
import com.blackneko.domain.product.ProductRepository
import com.blackneko.infrastructure.database.mapper.toProduct
import com.blackneko.infrastructure.database.table.CategoriesTable
import com.blackneko.infrastructure.database.table.ProductsTable
import com.blackneko.infrastructure.database.table.RestaurantsTable
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.let

class ProductRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    ProductRepository {

    override suspend fun findById(
        id: UUID
    ): Product? =
        transactionRunner.transaction {

            ProductsTable
                .selectAll()
                .where {
                    ProductsTable.id eq id
                }
                .singleOrNull()
                ?.toProduct()
        }

    override suspend fun findByRestaurant(
        restaurantId: UUID
    ): List<Product> =
        transactionRunner.transaction {

            ProductsTable
                .selectAll()
                .where {
                    ProductsTable.restaurant eq
                            EntityID(
                                restaurantId,
                                RestaurantsTable
                            )
                }
                .map {
                    it.toProduct()
                }
        }

    override suspend fun findByCategory(
        categoryId: UUID
    ): List<Product> =
        transactionRunner.transaction {

            ProductsTable
                .selectAll()
                .where {
                    ProductsTable.category eq
                            EntityID(
                                categoryId,
                                CategoriesTable
                            )
                }
                .map {
                    it.toProduct()
                }
        }

    override suspend fun findAvailableByRestaurant(
        restaurantId: UUID
    ): List<Product> =
        transactionRunner.transaction {

            ProductsTable
                .selectAll()
                .where {

                    (ProductsTable.restaurant eq
                            EntityID(
                                restaurantId,
                                RestaurantsTable
                            )) and

                            (ProductsTable.isAvailable eq true)
                }
                .map {
                    it.toProduct()
                }
        }

    override suspend fun search(
        filter: ProductFilter
    ): List<Product> =
        transactionRunner.transaction {

            ProductsTable
                .selectAll()
                .where {
                    buildFilter(filter)
                }
                .orderBy(ProductsTable.id, org.jetbrains.exposed.v1.core.SortOrder.ASC)
                .limit(filter.page.limit).offset(filter.page.offset.toLong())
                .map {
                    it.toProduct()
                }
        }

    override suspend fun save(
        product: Product
    ): Product =
        transactionRunner.transaction {

            ProductsTable.insert {

                it[id] =
                    product.id

                it[restaurant] =
                    EntityID(
                        product.restaurantId,
                        RestaurantsTable
                    )

                it[category] =
                    product.categoryId?.let {
                            categoryId ->
                        EntityID(
                            categoryId,
                            CategoriesTable
                        )
                    }

                it[name] =
                    product.name

                it[description] =
                    product.description

                it[price] =
                    product.price.cents

                it[isAvailable] =
                    product.isAvailable

                it[createdAt] =
                    product.createdAt

                it[updatedAt] =
                    product.updatedAt
            }

            product
        }

    override suspend fun update(
        product: Product
    ): Product =
        transactionRunner.transaction {

            ProductsTable.update(
                where = {
                    ProductsTable.id eq
                            product.id
                }
            ) {

                it[restaurant] =
                    EntityID(
                        product.restaurantId,
                        RestaurantsTable
                    )

                it[category] =
                    product.categoryId?.let {
                            categoryId ->
                        EntityID(
                            categoryId,
                            CategoriesTable
                        )
                    }

                it[name] =
                    product.name

                it[description] =
                    product.description

                it[price] =
                    product.price.cents

                it[isAvailable] =
                    product.isAvailable

                it[updatedAt] =
                    product.updatedAt
            }

            product
        }

    override suspend fun delete(
        id: UUID
    ) {
        transactionRunner.transaction {

            ProductsTable.deleteWhere {
                ProductsTable.id eq id
            }
        }
    }

    private fun buildFilter(
        filter: ProductFilter
    ): Op<Boolean> {

        var condition: Op<Boolean> =
            ProductsTable.restaurant eq
                    EntityID(
                        filter.restaurantId,
                        RestaurantsTable
                    )

        filter.categoryId?.let {
                categoryId ->

            condition =
                condition and
                        (
                                ProductsTable.category eq
                                        EntityID(
                                            categoryId,
                                            CategoriesTable
                                        )
                                )
        }

        filter.available?.let {
                available ->

            condition =
                condition and
                        (
                                ProductsTable.isAvailable eq
                                        available
                                )
        }

        filter.search
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
            ?.let {
                    search ->

                condition =
                    condition and
                            (
                                    ProductsTable.name like
                                            "%$search%"
                                    )
            }

        return condition
    }
}
