package com.blackneko.presentation.marketplace

import com.blackneko.application.exception.ValidationException
import com.blackneko.application.marketplace.*
import com.blackneko.domain.restaurant.RestaurantStatus
import com.blackneko.domain.order.OrderStatus
import com.blackneko.domain.driver.DriverStatus
import com.blackneko.domain.user.Role
import com.blackneko.domain.product.ProductFilter
import com.blackneko.domain.shared.PageRequest
import com.blackneko.presentation.auth.dto.MeResponse
import com.blackneko.presentation.security.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

internal fun uuid(value: String, field: String): UUID = try { UUID.fromString(value) }
    catch (_: IllegalArgumentException) { throw ValidationException("Invalid UUID", field) }
private fun ApplicationCall.id(field: String = "id") = uuid(parameters[field] ?: throw ValidationException("Missing $field", field), field)
private fun ApplicationCall.actor() = authenticatedUser().userId
private fun ApplicationCall.page(): PageRequest {
    fun integer(name: String, default: Int): Int = request.queryParameters[name]?.let {
        it.toIntOrNull() ?: throw ValidationException("Invalid $name", name)
    } ?: default
    val limit = integer("limit", 20)
    val offset = integer("offset", 0)
    if (limit !in 1..100 || offset !in 0..100_000) throw ValidationException("Invalid pagination")
    return PageRequest(limit, offset)
}
private inline fun <reified T : Enum<T>> enum(value: String, field: String = "status"): T =
    enumValues<T>().firstOrNull { it.name == value } ?: throw ValidationException("Invalid $field", field)

fun Route.marketplaceRoutes() {
    val addresses by inject<AddressUseCases>()
    val catalog by inject<CatalogUseCases>()
    val orders by inject<OrderUseCases>()
    val driver by inject<DriverUseCases>()
    val admin by inject<AdminUseCases>()

    get("/restaurants") { call.respond(catalog.list(call.page()).map { it.response() }) }
    get("/restaurants/{id}") { call.respond(catalog.detail(call.id()).response()) }
    get("/restaurants/{id}/categories") { call.respond(catalog.listCategories(call.id(), call.page()).map { it.response() }) }
    get("/restaurants/{id}/products") {
        val available = call.request.queryParameters["available"]?.let {
            it.toBooleanStrictOrNull() ?: throw ValidationException("Invalid available", "available")
        }
        val filter = ProductFilter(call.id(), call.request.queryParameters["categoryId"]?.let { uuid(it, "categoryId") },
            available, call.request.queryParameters["search"], call.page())
        call.respond(catalog.search(filter).map { it.response() })
    }

    authenticate(JWT_AUTH) {
        get("/users/me") {
            val user = inject<com.blackneko.application.auth.GetCurrentUserUseCase>().value(call.actor())
            call.respond(MeResponse(user.id.toString(), user.email, user.phone, user.firstName, user.lastName, user.roles.map { it.name }, user.isActive))
        }
        route("/addresses") {
            get { call.respond(addresses.list(call.actor(), call.page()).map { it.response() }) }
            post { call.respond(HttpStatusCode.Created, addresses.create(call.actor(), call.receive<AddressRequest>().command()).response()) }
            get("/{id}") { call.respond(addresses.get(call.actor(), call.id()).response()) }
            put("/{id}") { call.respond(addresses.update(call.actor(), call.id(), call.receive<AddressRequest>().command()).response()) }
            delete("/{id}") { addresses.delete(call.actor(), call.id()); call.respond(HttpStatusCode.NoContent) }
            post("/{id}/default") { call.respond(addresses.setDefault(call.actor(), call.id()).response()) }
        }
        route("/restaurants") {
            get("/mine") { call.respond(catalog.owned(call.actor(), call.page()).map { it.response() }) }
            post { call.respond(HttpStatusCode.Created, catalog.createRestaurant(call.actor(), call.receive<RestaurantRequest>().command()).response()) }
            get("/{id}/manage") { call.respond(catalog.manageDetail(call.actor(), call.id()).response()) }
            put("/{id}") { call.respond(catalog.updateRestaurant(call.actor(), call.id(), call.receive<RestaurantRequest>().command()).response()) }
            patch("/{id}/status") { call.respond(catalog.restaurantStatus(call.actor(), call.id(), enum<RestaurantStatus>(call.receive<StatusRequest>().status)).response()) }
            get("/{id}/management/categories") { call.respond(catalog.managedCategories(call.actor(), call.id(), call.page()).map { it.response() }) }
            post("/{id}/categories") { call.respond(HttpStatusCode.Created, catalog.category(call.actor(), call.id(), null, call.receive<CategoryRequest>().command()).response()) }
            put("/{id}/categories/{categoryId}") { call.respond(catalog.category(call.actor(), call.id(), call.id("categoryId"), call.receive<CategoryRequest>().command()).response()) }
            patch("/{id}/categories/{categoryId}/active") { call.respond(catalog.categoryActive(call.actor(), call.id(), call.id("categoryId"), call.receive<ActiveRequest>().isActive).response()) }
            post("/{id}/products") { call.respond(HttpStatusCode.Created, catalog.product(call.actor(), call.id(), null, call.receive<ProductRequest>().command()).response()) }
            put("/{id}/products/{productId}") { call.respond(catalog.product(call.actor(), call.id(), call.id("productId"), call.receive<ProductRequest>().command()).response()) }
            patch("/{id}/products/{productId}/availability") { call.respond(catalog.productAvailability(call.actor(), call.id(), call.id("productId"), call.receive<AvailabilityRequest>().isAvailable).response()) }
            get("/{id}/orders") { call.respond(orders.restaurantOrders(call.actor(), call.id(), call.page()).map { it.response() }) }
        }
        route("/orders") {
            get { call.respond(orders.own(call.actor(), call.page()).map { it.response() }) }
            post { call.respond(HttpStatusCode.Created, orders.create(call.actor(), call.receive<OrderRequest>().command(), call.request.headers["Idempotency-Key"]).response()) }
            get("/{id}") { call.respond(orders.detail(call.actor(), call.id()).response()) }
            post("/{id}/status") { call.respond(orders.transition(call.actor(), call.id(), enum<OrderStatus>(call.receive<StatusRequest>().status)).response()) }
        }
        route("/driver") {
            get("/me") { call.respond(driver.me(call.actor()).response()) }
            patch("/status") { call.respond(driver.status(call.actor(), enum<DriverStatus>(call.receive<StatusRequest>().status)).response()) }
            get("/orders") { call.respond(orders.assigned(call.actor(), call.page()).map { it.response() }) }
            get("/orders/available") { call.respond(orders.available(call.actor(), call.page()).map { it.response() }) }
            post("/orders/{id}/accept") { call.respond(orders.accept(call.actor(), call.id()).response()) }
        }
        route("/admin") {
            get("/users") { call.respond(admin.list(call.actor(), call.page()).map {
                MeResponse(it.id.toString(), it.email, it.phone, it.firstName, it.lastName, it.roles.map { role -> role.name }, it.isActive)
            }) }
            put("/users/{id}") {
                val request = call.receive<AdminUserRequest>()
                val user = admin.user(call.actor(), call.id(), request.roles.map { enum<Role>(it, "roles") }.toSet(), request.isActive)
                call.respond(MeResponse(user.id.toString(), user.email, user.phone, user.firstName, user.lastName, user.roles.map { it.name }, user.isActive))
            }
            patch("/drivers/{id}/status") { call.respond(admin.driverStatus(call.actor(), call.id(), enum<DriverStatus>(call.receive<StatusRequest>().status)).response()) }
            patch("/restaurants/{id}/status") { call.respond(catalog.restaurantStatus(call.actor(), call.id(), enum<RestaurantStatus>(call.receive<StatusRequest>().status), administrative = true).response()) }
        }
    }
}
