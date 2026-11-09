package com.upchiapas.mitiendita.products.presentation.list

import com.upchiapas.mitiendita.products.domain.model.Product

/** Estado inmutable de Home (lista de productos). */
data class ProductListUiState(
    val userName: String = "",
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
    val productToDelete: Product? = null,
    val isDeleting: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val userMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val sessionExpired: Boolean = false
) {
    /** Carga inicial: aún no hay nada que mostrar. */
    val showFullScreenLoading: Boolean get() = isLoading && products.isEmpty()

    /** Error sin datos que mostrar: pantalla de error con "Reintentar". */
    val showFullScreenError: Boolean get() = !isLoading && errorMessage != null && products.isEmpty()

    /** La API respondió bien pero no hay productos. */
    val showEmpty: Boolean get() = !isLoading && hasLoaded && errorMessage == null && products.isEmpty()

    /** Hay lista visible y además se está trabajando (recarga o eliminación). */
    val isBusy: Boolean get() = isLoading || isDeleting

    val summary: String
        get() = if (products.size == 1) "1 producto registrado" else "${products.size} productos registrados"
}
