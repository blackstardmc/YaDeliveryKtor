package com.blackneko.integration

import com.blackneko.application.marketplace.*
import com.blackneko.application.exception.*
import com.blackneko.domain.order.*
import com.blackneko.domain.driver.DriverStatus
import com.blackneko.domain.shared.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.*

class MarketplaceIntegrationTest : MarketplaceFixture() {
    @Test
    fun `server prices snapshots idempotency and ownership`() : Unit = runBlocking {
        val seed = seed()
        val useCases = orderCases()
        val result = useCases.create(seed.customer.id, seed.command(), "first-order")
        assertEquals(Money(3000), result.order.subtotal)
        assertEquals(Money(3500), result.order.total)
        assertEquals(OrderStatus.RECEIVED, result.order.status)
        assertEquals(1, result.history.size)
        products.update(seed.product.copy(price = Money(9999), name = "Changed"))
        assertEquals(result.order.id, useCases.create(seed.customer.id, seed.command(), "first-order").order.id)
        val snapshot = useCases.detail(seed.customer.id, result.order.id).items.single()
        assertEquals("Pizza", snapshot.productName)
        assertEquals(Money(1500), snapshot.unitPrice)
        assertFailsWith<ConflictException> { useCases.create(seed.customer.id, seed.command().copy(notes = "different"), "first-order") }
        assertFailsWith<AuthorizationException> { useCases.detail(seed.otherCustomer.id, result.order.id) }
        assertFailsWith<AuthorizationException> { useCases.detail(seed.otherOwner.id, result.order.id) }
        assertFailsWith<AuthorizationException> { useCases.transition(seed.otherOwner.id, result.order.id, OrderStatus.CONFIRMED) }
        assertFailsWith<AuthorizationException> { useCases.transition(seed.driverA.userId, result.order.id, OrderStatus.DELIVERED) }
    }

    @Test
    fun `invalid orders never persist`() : Unit = runBlocking {
        val seed = seed()
        val useCases = orderCases()
        assertFailsWith<NotFoundException> { useCases.create(seed.customer.id, seed.command().copy(items = listOf(OrderLineCommand(UUID.randomUUID(), 1))), null) }
        assertFailsWith<ValidationException> { useCases.create(seed.customer.id, seed.command().copy(items = listOf(OrderLineCommand(seed.product.id, 0))), null) }
        assertFailsWith<ValidationException> { useCases.create(seed.customer.id, seed.command().copy(items = listOf(OrderLineCommand(seed.otherProduct.id, 1))), null) }
        assertFailsWith<AuthorizationException> { useCases.create(seed.customer.id, seed.command().copy(deliveryAddressId = seed.otherAddress.id), null) }
        products.update(seed.product.copy(isAvailable = false))
        assertFailsWith<ConflictException> { useCases.create(seed.customer.id, seed.command(), null) }
        assertTrue(orders.findByCustomer(seed.customer.id).isEmpty())
    }

    @Test
    fun `creation rolls back items order and idempotency on history failure`() : Unit = runBlocking {
        val seed = seed()
        val failure = object : OrderStatusHistoryRepository by history {
            override suspend fun save(history: OrderStatusHistory): OrderStatusHistory { throw IllegalStateException("Simulated history failure") }
        }
        assertFailsWith<IllegalStateException> { orderCases(failure).create(seed.customer.id, seed.command(), "rollback") }
        assertTrue(orders.findByCustomer(seed.customer.id).isEmpty())
        assertNull(requests.find(seed.customer.id, "rollback"))
    }

    @Test
    fun `workflow timestamps history delivery and terminal transitions`() : Unit = runBlocking {
        val seed = seed()
        val useCases = orderCases()
        val received = useCases.create(seed.customer.id, seed.command(), null)
        assertFailsWith<ConflictException> { useCases.transition(seed.owner.id, received.order.id, OrderStatus.READY) }
        useCases.transition(seed.owner.id, received.order.id, OrderStatus.CONFIRMED)
        useCases.transition(seed.owner.id, received.order.id, OrderStatus.PREPARING)
        val ready = useCases.transition(seed.owner.id, received.order.id, OrderStatus.READY)
        assertNotNull(ready.order.confirmedAt)
        assertNotNull(ready.order.preparingAt)
        assertNotNull(ready.order.readyAt)
        val accepted = useCases.accept(seed.driverA.userId, ready.order.id)
        assertEquals(OrderStatus.IN_DELIVERY, accepted.order.status)
        assertEquals(seed.driverA.id, accepted.order.driverId)
        assertEquals(DriverStatus.BUSY, drivers.findById(seed.driverA.id)?.status)
        assertEquals(accepted.order.id, useCases.accept(seed.driverA.userId, ready.order.id).order.id)
        assertFailsWith<AuthorizationException> { useCases.transition(seed.driverB.userId, ready.order.id, OrderStatus.DELIVERED) }
        val delivered = useCases.transition(seed.driverA.userId, ready.order.id, OrderStatus.DELIVERED)
        assertEquals(DriverStatus.AVAILABLE, drivers.findById(seed.driverA.id)?.status)
        assertNotNull(delivered.order.pickedUpAt)
        assertNotNull(delivered.order.deliveredAt)
        assertEquals(OrderStatus.entries.filter { it != OrderStatus.CANCELLED }, delivered.history.map { it.toStatus })
        assertEquals(delivered.order.deliveredAt, delivered.history.last().createdAt)
        assertFailsWith<ConflictException> { useCases.transition(seed.owner.id, ready.order.id, OrderStatus.PREPARING) }
    }

    @Test
    fun `cancellation allowed before readiness and terminal afterwards`() : Unit = runBlocking {
        val seed = seed()
        val useCases = orderCases()
        val received = useCases.create(seed.customer.id, seed.command(), null)
        val cancelled = useCases.transition(seed.customer.id, received.order.id, OrderStatus.CANCELLED)
        assertNotNull(cancelled.order.cancelledAt)
        assertEquals(OrderStatus.CANCELLED, cancelled.order.status)
        assertFailsWith<ConflictException> { useCases.transition(seed.owner.id, received.order.id, OrderStatus.CONFIRMED) }
        val ready = ready(seed)
        assertFailsWith<ConflictException> { useCases.transition(seed.customer.id, ready.order.id, OrderStatus.CANCELLED) }
    }

    @Test
    fun `two drivers accepting one order exactly one wins`() : Unit = runBlocking {
        val seed = seed()
        val ready = ready(seed)
        val gate = CompletableDeferred<Unit>()
        val results = withTimeout(20_000) {
            coroutineScope {
                val attempts = listOf(seed.driverA, seed.driverB).map { driver ->
                    async(Dispatchers.IO) { gate.await(); runCatching { orderCases().accept(driver.userId, ready.order.id) } }
                }
                gate.complete(Unit)
                attempts.awaitAll()
            }
        }
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, results.count { it.exceptionOrNull() is ConflictException })
        assertEquals(1, listOf(seed.driverA, seed.driverB).count { drivers.findById(it.id)?.status == DriverStatus.BUSY })
        assertEquals(5, history.findByOrder(ready.order.id).size)
    }

    @Test
    fun `one driver cannot accept two orders concurrently`() : Unit = runBlocking {
        val seed = seed()
        val readyA = ready(seed)
        val readyB = ready(seed)
        val results = withTimeout(20_000) {
            coroutineScope { listOf(readyA, readyB).map { async(Dispatchers.IO) { runCatching { orderCases().accept(seed.driverA.userId, it.order.id) } } }.awaitAll() }
        }
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, results.count { it.exceptionOrNull() is ConflictException })
        assertEquals(1, orders.findByDriver(seed.driverA.id).size)
    }

    @Test
    fun `address and catalog ownership defaults and pagination`() : Unit = runBlocking {
        val seed = seed()
        assertFailsWith<AuthorizationException> { addressCases.get(seed.otherCustomer.id, seed.address.id) }
        assertFailsWith<AuthorizationException> { addressCases.delete(seed.otherCustomer.id, seed.address.id) }
        assertFailsWith<AuthorizationException> { addressCases.setDefault(seed.otherCustomer.id, seed.address.id) }
        assertFailsWith<AuthorizationException> { catalog.product(seed.otherOwner.id, seed.restaurant.id, seed.product.id,
            ProductCommand(seed.category.id, "Changed", null, 10, true)) }
        assertFailsWith<AuthorizationException> { catalog.category(seed.otherOwner.id, seed.restaurant.id, seed.category.id,
            CategoryCommand("Changed", null, 0, false)) }
        val second = addressCases.create(seed.customer.id, AddressCommand("Work", "Street", null, null, "Habana", null, null, null, null, true))
        assertEquals(second.id, addresses.findDefaultByUser(seed.customer.id)?.id)
        coroutineScope { listOf(seed.address.id, second.id).map { async(Dispatchers.IO) { addressCases.setDefault(seed.customer.id, it) } }.awaitAll() }
        assertEquals(1, addresses.findByUser(seed.customer.id).count { it.isDefault })
        assertEquals(1, catalog.list(PageRequest(1)).size)
        assertEquals(1, catalog.list(PageRequest(1, 1)).size)
        assertTrue(catalog.list(PageRequest(1, 2)).isEmpty())
    }
}
