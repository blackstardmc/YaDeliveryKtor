package com.blackneko.domain.address

import com.blackneko.domain.product.Product
import java.util.UUID

interface AddressRepository {
    suspend fun isUsed(id: UUID): Boolean

    suspend fun findById(
        id: UUID
    ): Address?

    suspend fun findByUser(
        userId: UUID
    ): List<Address>

    suspend fun findDefaultByUser(
        userId: UUID
    ): Address?

    suspend fun save(
        address: Address
    ): Address

    suspend fun update(
        address: Address
    ): Address

    suspend fun delete(
        id: UUID
    )
}
