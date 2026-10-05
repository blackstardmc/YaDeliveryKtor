package com.blackneko.presentation.marketplace

import com.blackneko.application.marketplace.*
import com.blackneko.domain.address.Address
import com.blackneko.domain.restaurant.Restaurant
import com.blackneko.domain.category.Category
import com.blackneko.domain.product.Product
import com.blackneko.domain.driver.Driver
import com.blackneko.domain.order.Order
import kotlinx.serialization.Serializable

@Serializable
data class AddressRequest(val street: String, val city: String, val label: String? = null, val number: String? = null,
    val neighborhood: String? = null, val province: String? = null, val reference: String? = null,
    val latitude: Double? = null, val longitude: Double? = null, val isDefault: Boolean = false) {
    fun command() = AddressCommand(label, street, number, neighborhood, city, province, reference, latitude, longitude, isDefault)
}
@Serializable
data class AddressResponse(val id: String, val street: String, val city: String, val label: String?, val number: String?,
    val neighborhood: String?, val province: String?, val reference: String?, val latitude: Double?, val longitude: Double?, val isDefault: Boolean)
fun Address.response() = AddressResponse(id.toString(), street, city, label, number, neighborhood, province, reference, latitude, longitude, isDefault)

@Serializable
data class RestaurantRequest(val name: String, val addressId: String, val description: String? = null, val phone: String? = null) {
    fun command() = RestaurantCommand(name, description, phone, uuid(addressId, "addressId"))
}
@Serializable
data class RestaurantResponse(val id: String, val name: String, val description: String?, val phone: String?, val addressId: String, val status: String)
fun Restaurant.response() = RestaurantResponse(id.toString(), name, description, phone, addressId.toString(), status.name)

@Serializable
data class CategoryRequest(val name: String, val description: String? = null, val sortOrder: Int = 0, val isActive: Boolean = true) {
    fun command() = CategoryCommand(name, description, sortOrder, isActive)
}
@Serializable
data class CategoryResponse(val id: String, val restaurantId: String, val name: String, val description: String?, val sortOrder: Int, val isActive: Boolean)
fun Category.response() = CategoryResponse(id.toString(), restaurantId.toString(), name, description, sortOrder, isActive)

@Serializable
data class ProductRequest(val name: String, val priceCents: Long, val categoryId: String? = null, val description: String? = null, val isAvailable: Boolean = true) {
    fun command() = ProductCommand(categoryId?.let { uuid(it, "categoryId") }, name, description, priceCents, isAvailable)
}
@Serializable
data class ProductResponse(val id: String, val restaurantId: String, val categoryId: String?, val name: String, val description: String?, val priceCents: Long, val isAvailable: Boolean)
fun Product.response() = ProductResponse(id.toString(), restaurantId.toString(), categoryId?.toString(), name, description, price.cents, isAvailable)

@Serializable
data class OrderLineRequest(val productId: String, val quantity: Int)
@Serializable
data class OrderRequest(val restaurantId: String, val deliveryAddressId: String, val items: List<OrderLineRequest>, val notes: String? = null) {
    fun command() = CreateOrderCommand(uuid(restaurantId, "restaurantId"), uuid(deliveryAddressId, "deliveryAddressId"),
        items.map { OrderLineCommand(uuid(it.productId, "productId"), it.quantity) }, notes)
}
@Serializable
data class StatusRequest(val status: String)
@Serializable
data class AvailabilityRequest(val isAvailable: Boolean)
@Serializable
data class ActiveRequest(val isActive: Boolean)
@Serializable
data class AdminUserRequest(val roles: Set<String>, val isActive: Boolean)

@Serializable
data class OrderSummary(val id: String, val customerId: String, val restaurantId: String, val driverId: String?, val deliveryAddressId: String,
    val status: String, val subtotalCents: Long, val deliveryFeeCents: Long, val totalCents: Long, val notes: String?,
    val createdAt: String, val updatedAt: String, val confirmedAt: String?, val preparingAt: String?, val readyAt: String?,
    val pickedUpAt: String?, val deliveredAt: String?, val cancelledAt: String?, val paymentMethod: String = "CASH")
fun Order.response() = OrderSummary(id.toString(), customerId.toString(), restaurantId.toString(), driverId?.toString(),
    deliveryAddressId.toString(), status.name, subtotal.cents, deliveryFee.cents, total.cents, notes, createdAt.toString(), updatedAt.toString(),
    confirmedAt?.toString(), preparingAt?.toString(), readyAt?.toString(), pickedUpAt?.toString(), deliveredAt?.toString(), cancelledAt?.toString())
@Serializable
data class OrderItemResponse(val productId: String, val productName: String, val unitPriceCents: Long, val quantity: Int, val subtotalCents: Long)
@Serializable
data class OrderHistoryResponse(val fromStatus: String?, val toStatus: String, val changedBy: String?, val createdAt: String)
@Serializable
data class OrderResponse(val order: OrderSummary, val items: List<OrderItemResponse>, val history: List<OrderHistoryResponse>,
    val deliveryAddress: AddressResponse?, val pickupAddress: AddressResponse?, val customerPhone: String?)
fun OrderDetail.response() = OrderResponse(order.response(), items.map {
    OrderItemResponse(it.productId.toString(), it.productName, it.unitPrice.cents, it.quantity, it.subtotal.cents)
}, history.map { OrderHistoryResponse(it.fromStatus?.name, it.toStatus.name, it.changedBy?.toString(), it.createdAt.toString()) },
    deliveryAddress?.response(), pickupAddress?.response(), customerPhone)

@Serializable
data class DriverResponse(val id: String, val status: String, val vehicleType: String?, val vehicleDescription: String?)
fun Driver.response() = DriverResponse(id.toString(), status.name, vehicleType?.name, vehicleDescription)
