package com.upchiapas.mitiendita.products.data.repository

import com.upchiapas.mitiendita.core.network.safeApiCall
import com.upchiapas.mitiendita.products.data.mapper.toDomain
import com.upchiapas.mitiendita.products.data.mapper.toRequestDto
import com.upchiapas.mitiendita.products.data.remote.ProductApi
import com.upchiapas.mitiendita.products.domain.model.Product
import com.upchiapas.mitiendita.products.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementación del repositorio de productos contra la API REST.
 * Los DTOs nunca salen de esta clase: todo se convierte con el mapper.
 */
class ProductRepositoryImpl(
    private val api: ProductApi
) : ProductRepository {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    override val products: StateFlow<List<Product>> = _products.asStateFlow()

    override suspend fun refreshProducts(): Result<List<Product>> = safeApiCall {
        api.getProducts().toDomain().also { list -> _products.value = list }
    }

    override suspend fun getProduct(id: Int): Result<Product> = safeApiCall {
        api.getProduct(id).toDomain()
    }

    override suspend fun createProduct(product: Product): Result<Product> = safeApiCall {
        api.createProduct(product.toRequestDto()).toDomain().also { created ->
            _products.update { list -> list + created }
        }
    }

    override suspend fun updateProduct(product: Product): Result<Product> = safeApiCall {
        api.updateProduct(product.id, product.toRequestDto()).toDomain().also { updated ->
            _products.update { list -> list.map { if (it.id == updated.id) updated else it } }
        }
    }

    override suspend fun deleteProduct(id: Int): Result<Unit> = safeApiCall {
        api.deleteProduct(id)
        _products.update { list -> list.filterNot { it.id == id } }
    }
}
