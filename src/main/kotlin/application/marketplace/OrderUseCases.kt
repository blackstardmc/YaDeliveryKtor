package com.blackneko.application.marketplace

import com.blackneko.application.*
import com.blackneko.application.exception.*
import com.blackneko.domain.address.AddressRepository
import com.blackneko.domain.address.Address
import com.blackneko.domain.restaurant.RestaurantRepository
import com.blackneko.domain.product.ProductRepository
import com.blackneko.domain.driver.*
import com.blackneko.domain.order.*
import com.blackneko.domain.security.Permission
import com.blackneko.domain.shared.*
import com.blackneko.domain.user.Role
import java.time.Clock
import java.security.MessageDigest
import java.util.UUID

data class OrderLineCommand(val productId: UUID, val quantity: Int)
data class CreateOrderCommand(val restaurantId: UUID, val deliveryAddressId: UUID, val items: List<OrderLineCommand>, val notes: String?)
data class OrderDetail(val order: Order, val items: List<OrderItem>, val history: List<OrderStatusHistory>,
    val deliveryAddress: Address? = null, val pickupAddress: Address? = null, val customerPhone: String? = null)

class OrderUseCases(private val orders: OrderRepository, private val items: OrderItemRepository,
    private val history: OrderStatusHistoryRepository, private val requests: OrderRequestRepository,
    private val restaurants: RestaurantRepository, private val addresses: AddressRepository,
    private val products: ProductRepository, private val drivers: DriverRepository, private val access: AccessPolicy,
    private val transactions: TransactionRunner, private val locks: EntityLocks, private val clock: Clock,
    private val deliveryFee: Money = Money.ZERO) {

    suspend fun create(actor: UUID, command: CreateOrderCommand, key: String?): OrderDetail = transactions.transaction {
        locks.user(actor)
        access.require(actor, Permission.ORDER_CREATE)
        validate(command.items.size in 1..50, "items", "Order must contain 1 to 50 items")
        validate(command.items.map { it.productId }.distinct().size == command.items.size, "items", "Duplicate product")
        validate(command.items.all { it.quantity in 1..100 }, "quantity", "Quantity must be 1 to 100")
        optionalText(command.notes, "notes", 2000)
        validate(key == null || Regex("^[A-Za-z0-9_-]{1,100}$").matches(key), "Idempotency-Key", "Invalid idempotency key")
        val fingerprint = fingerprint(command)
        if (key != null) requests.find(actor, key)?.let {
            if (it.fingerprint != fingerprint) throw ConflictException("Idempotency key already used for a different request")
            return@transaction detail(actor, it.orderId)
        }
        locks.restaurant(command.restaurantId)
        val restaurant = restaurants.findById(command.restaurantId) ?: throw NotFoundException("Restaurant not found")
        if (!restaurant.canReceiveOrders()) throw ConflictException("Restaurant is not active")
        val address = addresses.findById(command.deliveryAddressId) ?: throw NotFoundException("Address not found")
        access.own(actor, address.userId)
        val id = UUID.randomUUID()
        val lines = command.items.map {
            val product = products.findById(it.productId) ?: throw NotFoundException("Product not found")
            validate(product.restaurantId == restaurant.id, "productId", "Product belongs to a different restaurant")
            if (!product.canBeOrdered()) throw ConflictException("Product is unavailable")
            OrderItem(UUID.randomUUID(), id, product.id, product.name, product.price, it.quantity, product.price * it.quantity)
        }
        val subtotal = lines.fold(Money.ZERO) { total, item -> total + item.subtotal }
        val now = clock.instant()
        val order = OrderFactory.create(id, actor, restaurant.id, address.id, subtotal, deliveryFee, command.notes)
            .copy(createdAt = now, updatedAt = now)
        orders.save(order)
        items.saveAll(lines)
        val initial = OrderStatusHistory(UUID.randomUUID(), id, null, OrderStatus.RECEIVED, actor, now)
        history.save(initial)
        if (key != null) requests.save(OrderRequest(actor, key, fingerprint, id))
        OrderDetail(order, lines, listOf(initial), address, addresses.findById(restaurant.addressId), access.contactPhone(actor))
    }

    suspend fun detail(actor: UUID, id: UUID): OrderDetail = transactions.transaction {
        val order = readable(actor, id)
        OrderDetail(order, items.findByOrder(id), history.findByOrder(id), addresses.findById(order.deliveryAddressId),
            restaurants.findById(order.restaurantId)?.let { addresses.findById(it.addressId) }, access.contactPhone(order.customerId))
    }

    private suspend fun readable(actor: UUID, id: UUID): Order {
        val user = access.actor(actor)
        val order = orders.findById(id) ?: throw NotFoundException("Order not found")
        val allowed = user.hasRole(Role.ADMIN) || (user.hasRole(Role.CUSTOMER) && order.customerId == actor) ||
            (user.hasRole(Role.RESTAURANT) && restaurants.findById(order.restaurantId)?.ownerId == actor) ||
            (user.hasRole(Role.DRIVER) && order.driverId != null && drivers.findByUserId(actor)?.id == order.driverId)
        if (!allowed) throw AuthorizationException()
        return order
    }

    suspend fun own(actor: UUID, page: PageRequest): List<Order> {
        access.require(actor, Permission.ORDER_READ_OWN)
        return orders.findByCustomer(actor, page)
    }
    suspend fun restaurantOrders(actor: UUID, restaurantId: UUID, page: PageRequest): List<Order> {
        access.restaurant(actor, restaurantId, Permission.RESTAURANT_ORDER_READ)
        return orders.findByRestaurant(restaurantId, page)
    }
    suspend fun available(actor: UUID, page: PageRequest): List<Order> {
        access.require(actor, Permission.DRIVER_ORDER_LIST)
        val driver = drivers.findByUserId(actor) ?: throw NotFoundException("Driver not found")
        if (!driver.isAvailable()) throw ConflictException("Driver is not available")
        return orders.findAvailableForDrivers(page)
    }
    suspend fun assigned(actor: UUID, page: PageRequest): List<Order> {
        access.require(actor, Permission.DRIVER_ORDER_LIST)
        val driver = drivers.findByUserId(actor) ?: throw NotFoundException("Driver not found")
        return orders.findByDriver(driver.id, page)
    }

    suspend fun transition(actor: UUID, id: UUID, target: OrderStatus): OrderDetail = transactions.transaction {
        locks.order(id)
        val order = readable(actor, id)
        val user = access.actor(actor)
        val permitted = when (target) {
            OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY -> {
                access.restaurant(actor, order.restaurantId, Permission.RESTAURANT_ORDER_UPDATE); true
            }
            OrderStatus.DELIVERED -> {
                access.require(actor, Permission.DRIVER_ORDER_UPDATE)
                order.driverId != null && drivers.findByUserId(actor)?.id == order.driverId
            }
            OrderStatus.CANCELLED -> user.hasRole(Role.ADMIN) ||
                (user.hasPermission(Permission.ORDER_CANCEL_OWN) && order.customerId == actor) ||
                (user.hasPermission(Permission.RESTAURANT_ORDER_UPDATE) && restaurants.findById(order.restaurantId)?.ownerId == actor)
            else -> false
        }
        if (!permitted) throw AuthorizationException()
        if (!OrderStatusTransitions.canTransition(order.status, target)) throw ConflictException("Invalid order status transition")
        val now = clock.instant()
        val updated = order.transitionTo(target, now)
        if (target == OrderStatus.DELIVERED) {
            val driver = drivers.findById(order.driverId!!) ?: throw NotFoundException("Driver not found")
            locks.driver(driver.userId)
            drivers.update(driver.copy(status = DriverStatus.AVAILABLE, updatedAt = now))
        }
        orders.update(updated)
        history.save(OrderStatusHistory(UUID.randomUUID(), id, order.status, target, actor, now))
        detail(actor, id)
    }

    suspend fun accept(actor: UUID, id: UUID): OrderDetail = transactions.transaction {
        locks.user(actor)
        access.require(actor, Permission.DRIVER_ORDER_ACCEPT)
        locks.order(id)
        val order = orders.findById(id) ?: throw NotFoundException("Order not found")
        val existing = drivers.findByUserId(actor) ?: throw NotFoundException("Driver not found")
        // Retrying an already successful acceptance returns the current assignment.
        if (order.driverId == existing.id && order.status == OrderStatus.IN_DELIVERY) return@transaction detail(actor, id)
        locks.driver(actor)
        val driver = drivers.findByUserId(actor) ?: throw NotFoundException("Driver not found")
        if (order.status != OrderStatus.READY || order.driverId != null || !driver.canAcceptOrder())
            throw ConflictException("Order or driver is no longer available")
        val now = clock.instant()
        orders.update(order.copy(driverId = driver.id).transitionTo(OrderStatus.IN_DELIVERY, now))
        drivers.update(driver.copy(status = DriverStatus.BUSY, updatedAt = now))
        history.save(OrderStatusHistory(UUID.randomUUID(), id, order.status, OrderStatus.IN_DELIVERY, actor, now))
        detail(actor, id)
    }

    private fun fingerprint(command: CreateOrderCommand): String {
        val notes = command.notes.orEmpty()
        val normalized = "${command.restaurantId}|${command.deliveryAddressId}|" +
            command.items.sortedBy { it.productId.toString() }.joinToString(";") { "${it.productId}:${it.quantity}" } + "|${notes.length}:$notes"
        return MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
