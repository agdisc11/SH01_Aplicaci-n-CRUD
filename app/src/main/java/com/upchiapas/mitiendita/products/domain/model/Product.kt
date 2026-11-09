package com.upchiapas.mitiendita.products.domain.model

import java.time.LocalDate

/**
 * Recurso principal del dominio "Tienda de abarrotes".
 * id + 5 atributos: texto (name, category), numéricos (price, stock) y fecha (expirationDate).
 */
data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val expirationDate: LocalDate
) {
    fun isExpired(today: LocalDate = LocalDate.now()): Boolean = expirationDate.isBefore(today)

    val isOutOfStock: Boolean get() = stock == 0

    val isLowStock: Boolean get() = stock in 1..LOW_STOCK_LIMIT

    companion object {
        const val NEW_PRODUCT_ID = 0
        const val LOW_STOCK_LIMIT = 5
    }
}
