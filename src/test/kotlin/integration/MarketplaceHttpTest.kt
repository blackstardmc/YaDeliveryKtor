package com.blackneko.integration

import com.blackneko.configureApplication
import com.blackneko.config.*
import com.blackneko.application.security.JwtTokenService
import com.blackneko.infrastructure.database.DatabaseConfig
import com.blackneko.infrastructure.security.JwtConfig
import com.blackneko.presentation.marketplace.*
import com.blackneko.domain.user.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class MarketplaceHttpTest : MarketplaceFixture() {
    private val jwt = JwtConfig("market-test", "market-client", "Test", "marketplace-http-test-secret-at-least-32", 15, 30)
    private val tokenService = JwtTokenService(jwt)
    private fun ApplicationTestBuilder.setup() {
        environment { config = MapApplicationConfig() }
        application {
            configureApplication(listOf(infrastructureModule(DatabaseConfig(postgres.host, postgres.firstMappedPort,
                postgres.databaseName, postgres.username, postgres.password, 5), dataSource, database),
                securityModule(jwt), marketplaceModule(500)), authLimit = 100)
        }
    }
    private fun ApplicationTestBuilder.jsonClient() = createClient { install(ContentNegotiation) { json() } }
    private fun HttpRequestBuilder.actor(user: User) { bearerAuth(tokenService.generateAccessToken(user)) }

    @Test
    fun `HTTP marketplace creates catalog orders assignment and delivery`() = testApplication {
        val seed = seed()
        setup()
        val http = jsonClient()
        assertEquals(HttpStatusCode.OK, http.get("/openapi.json").status)
        assertEquals(HttpStatusCode.OK, http.get("/swagger").status)
        val addressCreated = http.post("/addresses") {
            actor(seed.customer); contentType(ContentType.Application.Json)
            setBody(AddressRequest("Customer Street", "Habana", isDefault = true))
        }
        assertEquals(HttpStatusCode.Created, addressCreated.status)
        val address = addressCreated.body<AddressResponse>()
        val updatedAddress = http.put("/addresses/${address.id}") {
            actor(seed.customer); contentType(ContentType.Application.Json)
            setBody(AddressRequest("Updated Street", "Habana", isDefault = true))
        }
        assertEquals(HttpStatusCode.OK, updatedAddress.status)
        val restaurantCreated = http.post("/restaurants") {
            actor(seed.owner); contentType(ContentType.Application.Json)
            setBody(RestaurantRequest("New Restaurant", restaurants.findById(seed.restaurant.id)!!.addressId.toString()))
        }
        assertEquals(HttpStatusCode.Created, restaurantCreated.status)
        val restaurant = restaurantCreated.body<RestaurantResponse>()
        assertEquals("INACTIVE", restaurant.status)
        assertEquals(HttpStatusCode.OK, http.patch("/restaurants/${restaurant.id}/status") {
            actor(seed.owner); contentType(ContentType.Application.Json); setBody(StatusRequest("ACTIVE"))
        }.status)
        assertEquals(HttpStatusCode.OK, http.get("/restaurants/mine") { actor(seed.owner) }.status)
        val categoryCreated = http.post("/restaurants/${restaurant.id}/categories") {
            actor(seed.owner); contentType(ContentType.Application.Json); setBody(CategoryRequest("Main"))
        }
        assertEquals(HttpStatusCode.Created, categoryCreated.status)
        val category = categoryCreated.body<CategoryResponse>()
        val productCreated = http.post("/restaurants/${restaurant.id}/products") {
            actor(seed.owner); contentType(ContentType.Application.Json); setBody(ProductRequest("Pizza", 1500, category.id))
        }
        assertEquals(HttpStatusCode.Created, productCreated.status)
        val product = productCreated.body<ProductResponse>()
        assertEquals(1, http.get("/restaurants/${restaurant.id}/products?search=Pizza&available=true").body<List<ProductResponse>>().size)

        val request = OrderRequest(restaurant.id, address.id, listOf(OrderLineRequest(product.id, 2)), "Call first")
        val created = http.post("/orders") {
            actor(seed.customer); contentType(ContentType.Application.Json); header("Idempotency-Key", "http-first-order"); setBody(request)
        }
        assertEquals(HttpStatusCode.Created, created.status)
        val order = created.body<OrderResponse>()
        assertEquals(3500, order.order.totalCents)
        assertEquals("CASH", order.order.paymentMethod)
        val replay = http.post("/orders") {
            actor(seed.customer); contentType(ContentType.Application.Json); header("Idempotency-Key", "http-first-order"); setBody(request)
        }.body<OrderResponse>()
        assertEquals(order.order.id, replay.order.id)
        assertEquals(HttpStatusCode.OK, http.get("/orders/${order.order.id}") { actor(seed.customer) }.status)
        assertEquals(HttpStatusCode.OK, http.get("/restaurants/${restaurant.id}/orders") { actor(seed.owner) }.status)
        listOf("CONFIRMED", "PREPARING", "READY").forEach { status ->
            assertEquals(HttpStatusCode.OK, http.post("/orders/${order.order.id}/status") {
                actor(seed.owner); contentType(ContentType.Application.Json); setBody(StatusRequest(status))
            }.status)
        }
        assertEquals(1, http.get("/driver/orders/available") { actor(users.findById(seed.driverA.userId)!!) }.body<List<OrderSummary>>().size)
        val driver = users.findById(seed.driverA.userId)!!
        val accepted = http.post("/driver/orders/${order.order.id}/accept") { actor(driver) }
        assertEquals(HttpStatusCode.OK, accepted.status)
        assertEquals("IN_DELIVERY", accepted.body<OrderResponse>().order.status)
        assertEquals("Updated Street", accepted.body<OrderResponse>().deliveryAddress?.street)
        assertEquals(seed.customer.phone, accepted.body<OrderResponse>().customerPhone)
        assertNotNull(accepted.body<OrderResponse>().pickupAddress)
        assertEquals(1, http.get("/driver/orders") { actor(driver) }.body<List<OrderSummary>>().size)
        val delivered = http.post("/orders/${order.order.id}/status") {
            actor(driver); contentType(ContentType.Application.Json); setBody(StatusRequest("DELIVERED"))
        }
        assertEquals(HttpStatusCode.OK, delivered.status)
        assertEquals(6, delivered.body<OrderResponse>().history.size)
        assertEquals("AVAILABLE", http.get("/driver/me") { actor(driver) }.body<DriverResponse>().status)
        assertEquals(HttpStatusCode.Conflict, http.delete("/addresses/${address.id}") { actor(seed.customer) }.status)
    }

    @Test
    fun `HTTP IDOR malformed identifiers privilege and money injection rejected`() = testApplication {
        val seed = seed()
        val existing = orderCases().create(seed.customer.id, seed.command(), null)
        setup()
        val http = jsonClient()
        assertEquals(HttpStatusCode.Unauthorized, http.post("/orders").status)
        assertEquals(HttpStatusCode.Forbidden, http.get("/orders/${existing.order.id}") { actor(seed.otherCustomer) }.status)
        assertEquals(HttpStatusCode.Forbidden, http.get("/addresses/${seed.address.id}") { actor(seed.otherCustomer) }.status)
        assertEquals(HttpStatusCode.Forbidden, http.put("/addresses/${seed.address.id}") {
            actor(seed.otherCustomer); contentType(ContentType.Application.Json); setBody(AddressRequest("Street", "Habana"))
        }.status)
        assertEquals(HttpStatusCode.Forbidden, http.put("/restaurants/${seed.restaurant.id}/products/${seed.product.id}") {
            actor(seed.otherOwner); contentType(ContentType.Application.Json); setBody(ProductRequest("Changed", 1))
        }.status)
        assertEquals(HttpStatusCode.Forbidden, http.put("/restaurants/${seed.restaurant.id}/categories/${seed.category.id}") {
            actor(seed.otherOwner); contentType(ContentType.Application.Json); setBody(CategoryRequest("Changed"))
        }.status)
        assertEquals(HttpStatusCode.Forbidden, http.get("/admin/users") { actor(seed.customer) }.status)
        assertEquals(HttpStatusCode.Unauthorized, http.get("/admin/users").status)
        assertEquals(HttpStatusCode.BadRequest, http.get("/orders/not-a-uuid") { actor(seed.customer) }.status)
        assertEquals(HttpStatusCode.BadRequest, http.get("/restaurants?limit=101").status)
        val injected = """{"restaurantId":"${seed.restaurant.id}","deliveryAddressId":"${seed.address.id}","items":[{"productId":"${seed.product.id}","quantity":1}],"price":1,"total":1,"status":"DELIVERED","customerId":"${seed.otherCustomer.id}","driverId":"${seed.driverA.id}"}"""
        assertEquals(HttpStatusCode.BadRequest, http.post("/orders") {
            actor(seed.customer); contentType(ContentType.Application.Json); setBody(injected)
        }.status)
        // Authorization uses the current DB roles, even when an old signed token still carries ADMIN.
        val forgedRoles = seed.customer.copy(roles = setOf(Role.ADMIN))
        assertEquals(HttpStatusCode.Forbidden, http.get("/admin/users") { actor(forgedRoles) }.status)
        users.update(seed.customer.copy(isActive = false))
        assertEquals(HttpStatusCode.Unauthorized, http.get("/orders") { actor(seed.customer) }.status)
        assertEquals(HttpStatusCode.Unauthorized, http.get("/users/me") { actor(seed.customer) }.status)
    }

    @Test
    fun `admin provisions driver and revokes role sessions`() = testApplication {
        val seed = seed()
        users.update(seed.owner.copy(roles = setOf(Role.ADMIN)))
        val admin = users.findById(seed.owner.id)!!
        setup()
        val http = jsonClient()
        assertEquals(HttpStatusCode.OK, http.get("/admin/users?limit=2") { actor(admin) }.status)
        assertEquals(HttpStatusCode.OK, http.put("/admin/users/${seed.otherCustomer.id}") {
            actor(admin); contentType(ContentType.Application.Json); setBody(AdminUserRequest(setOf("CUSTOMER", "DRIVER"), true))
        }.status)
        assertNotNull(drivers.findByUserId(seed.otherCustomer.id))
        val driver = users.findById(seed.otherCustomer.id)!!
        assertEquals(HttpStatusCode.OK, http.patch("/driver/status") {
            actor(driver); contentType(ContentType.Application.Json); setBody(StatusRequest("AVAILABLE"))
        }.status)
        assertEquals(HttpStatusCode.OK, http.patch("/admin/drivers/${driver.id}/status") {
            actor(admin); contentType(ContentType.Application.Json); setBody(StatusRequest("SUSPENDED"))
        }.status)
        assertEquals(HttpStatusCode.Conflict, http.patch("/driver/status") {
            actor(driver); contentType(ContentType.Application.Json); setBody(StatusRequest("AVAILABLE"))
        }.status)
    }
}
