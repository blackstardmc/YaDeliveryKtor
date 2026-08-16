package com.blackneko.domain.address

import java.util.UUID
import java.time.Instant

data class Address(
    val id: UUID,
    val userId: UUID,
    val label: String?,
    val street: String,
    val number: String?,
    val neighborhood: String?,
    val city: String,
    val province: String?,
    val reference: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isDefault: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
){
    init {
        require(street.isNotBlank()) {
            "Street cannot be blank"
        }

        require(city.isNotBlank()) {
            "City cannot be blank"
        }

        latitude?.let {
            require(it in -90.0..90.0) {
                "Invalid latitude"
            }
        }

        longitude?.let {
            require(it in -180.0..180.0) {
                "Invalid longitude"
            }
        }
    }

    fun hasCoordinates(): Boolean {
        return latitude != null &&
                longitude != null
    }
}