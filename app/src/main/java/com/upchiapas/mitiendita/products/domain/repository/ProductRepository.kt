package com.upchiapas.mitiendita.products.domain.repository

import com.upchiapas.mitiendita.products.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato del CRUD de productos. Solo expone modelos de dominio.
 *
 * [products] es la fuente única de verdad en memoria: cada operación exitosa
 * (crear, editar, eliminar) la actualiza, así todas las pantallas que la observan
 * se refrescan automáticamente.
 */
interface ProductRepository {
    val products: StateFlow<List<Product>>
    suspend fun refreshProducts(): Result<List<Product>>
    suspend fun getProduct(id: Int): Result<Product>
    suspend fun createProduct(product: Product): Result<Product>
    suspend fun updateProduct(product: Product): Result<Product>
    suspend fun deleteProduct(id: Int): Result<Unit>
}
