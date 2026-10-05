package com.blackneko.application.marketplace

import com.blackneko.application.*
import com.blackneko.application.exception.*
import com.blackneko.domain.address.AddressRepository
import com.blackneko.domain.restaurant.*
import com.blackneko.domain.category.*
import com.blackneko.domain.product.*
import com.blackneko.domain.security.Permission
import com.blackneko.domain.shared.*
import com.blackneko.domain.user.Role
import java.time.Clock
import java.util.UUID

data class RestaurantCommand(val name: String, val description: String?, val phone: String?, val addressId: UUID)
data class CategoryCommand(val name: String, val description: String?, val sortOrder: Int, val isActive: Boolean)
data class ProductCommand(val categoryId: UUID?, val name: String, val description: String?, val priceCents: Long, val isAvailable: Boolean)

class CatalogUseCases(private val restaurants: RestaurantRepository, private val categories: CategoryRepository,
    private val products: ProductRepository, private val addresses: AddressRepository, private val access: AccessPolicy,
    private val transactions: TransactionRunner, private val locks: EntityLocks, private val clock: Clock) {

    suspend fun list(page: PageRequest) = restaurants.findAllActive(page)
    suspend fun detail(id: UUID): Restaurant {
        val restaurant = restaurants.findById(id) ?: throw NotFoundException("Restaurant not found")
        if (!restaurant.isActive()) throw NotFoundException("Restaurant not found")
        return restaurant
    }
    suspend fun owned(actor: UUID, page: PageRequest): List<Restaurant> {
        access.require(actor, Permission.RESTAURANT_UPDATE)
        return restaurants.findByOwner(actor, page)
    }
    suspend fun manageDetail(actor: UUID, id: UUID) = access.restaurant(actor, id, Permission.RESTAURANT_UPDATE)

    suspend fun createRestaurant(actor: UUID, command: RestaurantCommand): Restaurant = transactions.transaction {
        locks.user(actor)
        access.require(actor, Permission.RESTAURANT_CREATE)
        val address = addresses.findById(command.addressId) ?: throw NotFoundException("Address not found")
        access.own(actor, address.userId)
        val now = clock.instant()
        restaurants.save(Restaurant(UUID.randomUUID(), actor, text(command.name, "name", 200),
            optionalText(command.description, "description", 4000), optionalText(command.phone, "phone", 20),
            command.addressId, RestaurantStatus.INACTIVE, now, now))
    }

    suspend fun updateRestaurant(actor: UUID, id: UUID, command: RestaurantCommand): Restaurant = transactions.transaction {
        val owned = access.restaurant(actor, id, Permission.RESTAURANT_UPDATE)
        locks.user(owned.ownerId)
        locks.restaurant(id)
        val restaurant = access.restaurant(actor, id, Permission.RESTAURANT_UPDATE)
        val address = addresses.findById(command.addressId) ?: throw NotFoundException("Address not found")
        access.own(restaurant.ownerId, address.userId)
        restaurants.update(restaurant.copy(name = text(command.name, "name", 200),
            description = optionalText(command.description, "description", 4000), phone = optionalText(command.phone, "phone", 20),
            addressId = address.id, updatedAt = clock.instant()))
    }

    suspend fun restaurantStatus(actor: UUID, id: UUID, status: RestaurantStatus, administrative: Boolean = false): Restaurant = transactions.transaction {
        if (administrative) access.require(actor, Permission.ADMIN_RESTAURANT_MANAGE)
        locks.restaurant(id)
        val user = access.actor(actor)
        val restaurant = access.restaurant(actor, id, Permission.RESTAURANT_UPDATE)
        if (!user.hasRole(Role.ADMIN) && (status == RestaurantStatus.SUSPENDED || restaurant.status == RestaurantStatus.SUSPENDED))
            throw AuthorizationException()
        restaurants.update(restaurant.copy(status = status, updatedAt = clock.instant()))
    }

    suspend fun listCategories(restaurantId: UUID, page: PageRequest): List<Category> {
        detail(restaurantId)
        return categories.findByRestaurant(restaurantId, page, activeOnly = true)
    }
    suspend fun managedCategories(actor: UUID, restaurantId: UUID, page: PageRequest): List<Category> {
        access.restaurant(actor, restaurantId, Permission.CATEGORY_MANAGE)
        return categories.findByRestaurant(restaurantId, page)
    }
    suspend fun category(actor: UUID, restaurantId: UUID, id: UUID?, command: CategoryCommand): Category = transactions.transaction {
        locks.restaurant(restaurantId)
        access.restaurant(actor, restaurantId, Permission.CATEGORY_MANAGE)
        validate(command.sortOrder >= 0, "sortOrder", "Sort order cannot be negative")
        val original = id?.let { categories.findById(it) ?: throw NotFoundException("Category not found") }
        if (original != null && original.restaurantId != restaurantId) throw AuthorizationException()
        val now = clock.instant()
        val category = Category(id ?: UUID.randomUUID(), restaurantId, text(command.name, "name", 120),
            optionalText(command.description, "description", 4000), command.sortOrder, command.isActive, original?.createdAt ?: now, now)
        if (original == null) categories.save(category) else categories.update(category)
    }

    suspend fun search(filter: ProductFilter): List<Product> {
        detail(filter.restaurantId)
        validate(filter.search == null || filter.search.length <= 200, "search", "Search is too long")
        return products.search(filter)
    }

    suspend fun categoryActive(actor: UUID, restaurantId: UUID, id: UUID, active: Boolean): Category = transactions.transaction {
        locks.restaurant(restaurantId)
        access.restaurant(actor, restaurantId, Permission.CATEGORY_MANAGE)
        val category = categories.findById(id) ?: throw NotFoundException("Category not found")
        if (category.restaurantId != restaurantId) throw AuthorizationException()
        categories.update(category.copy(isActive = active, updatedAt = clock.instant()))
    }
    suspend fun productAvailability(actor: UUID, restaurantId: UUID, id: UUID, available: Boolean): Product = transactions.transaction {
        locks.restaurant(restaurantId)
        access.restaurant(actor, restaurantId, Permission.PRODUCT_UPDATE)
        val product = products.findById(id) ?: throw NotFoundException("Product not found")
        if (product.restaurantId != restaurantId) throw AuthorizationException()
        products.update(product.copy(isAvailable = available, updatedAt = clock.instant()))
    }
    suspend fun product(actor: UUID, restaurantId: UUID, id: UUID?, command: ProductCommand): Product = transactions.transaction {
        locks.restaurant(restaurantId)
        access.restaurant(actor, restaurantId, Permission.PRODUCT_UPDATE)
        validate(command.priceCents in 1..1_000_000_000_000L, "priceCents", "Invalid price")
        command.categoryId?.let {
            val category = categories.findById(it) ?: throw NotFoundException("Category not found")
            if (category.restaurantId != restaurantId) throw AuthorizationException()
        }
        val original = id?.let { products.findById(it) ?: throw NotFoundException("Product not found") }
        if (original != null && original.restaurantId != restaurantId) throw AuthorizationException()
        val now = clock.instant()
        val product = Product(id ?: UUID.randomUUID(), restaurantId, command.categoryId, text(command.name, "name", 200),
            optionalText(command.description, "description", 4000), Money(command.priceCents), command.isAvailable, original?.createdAt ?: now, now)
        if (original == null) products.save(product) else products.update(product)
    }
}
