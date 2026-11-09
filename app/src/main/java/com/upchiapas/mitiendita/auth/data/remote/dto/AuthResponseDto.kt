package com.upchiapas.mitiendita.auth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Respuesta de /login y /register: { "accessToken": "...", "user": { ... } } */
@Serializable
data class AuthResponseDto(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("user") val user: UserDto
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("name") val name: String? = null
)
