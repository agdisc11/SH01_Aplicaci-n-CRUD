package com.upchiapas.mitiendita.navigation

import kotlinx.serialization.Serializable

/** Rutas type-safe de Navigation Compose. */

@Serializable
data class LoginRoute(val sessionExpired: Boolean = false)

@Serializable
data object RegisterRoute

/** Home = lista CRUD de productos. */
@Serializable
data object ProductListRoute

@Serializable
data class ProductDetailRoute(val productId: Int)

/** El mismo formulario se usa para crear (sin id)… */
@Serializable
data object ProductCreateRoute

/** …y para editar (con id). */
@Serializable
data class ProductEditRoute(val productId: Int)
