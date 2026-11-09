package com.upchiapas.mitiendita.products.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Objeto de transporte tal como llega de la API (la fecha viene como texto "yyyy-MM-dd"). */
@Serializable
data class ProductDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("price") val price: Double,
    @SerialName("stock") val stock: Int,
    @SerialName("expirationDate") val expirationDate: String
)
