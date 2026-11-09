package com.upchiapas.mitiendita.products.presentation.detail

import com.upchiapas.mitiendita.products.domain.model.Product

/** Estado inmutable de la pantalla de detalle. */
data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val userMessage: String? = null,
    val deletedMessage: String? = null,
    val sessionExpired: Boolean = false
)
