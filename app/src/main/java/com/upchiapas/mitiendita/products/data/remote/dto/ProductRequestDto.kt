package com.upchiapas.mitiendita.products.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Cuerpo que se envía al crear (POST) o actualizar (PUT). No lleva id. */
@Serializable
data class ProductRequestDto(
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("price") val price: Double,
    @SerialName("stock") val stock: Int,
    @SerialName("expirationDate") val expirationDate: String
)
